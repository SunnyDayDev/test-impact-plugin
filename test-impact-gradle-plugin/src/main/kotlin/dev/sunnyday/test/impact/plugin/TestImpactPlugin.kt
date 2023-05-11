package dev.sunnyday.test.impact.plugin

import dev.sunnyday.test.impact.plugin.config.TestImpactExtension
import dev.sunnyday.test.impact.plugin.config.UnchangedProjectTestStrategy
import dev.sunnyday.test.impact.plugin.graph.ProjectGraphResolver
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider

class TestImpactPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("testImpact", TestImpactExtension::class.java)

        val graphResolver = ProjectGraphResolver(target)

        val testImpactTaskProvider = addTestImpactTask(target, extension, graphResolver)

        if (!checkIsGradleStartedForTestImpact(target)) {
            return
        }

        evaluateProjectsGraph(target, extension, graphResolver)

        target.gradle.projectsEvaluated {
            graphResolver.markGraphCompleted()

            buildTestImpactTasksGraph(target, extension, graphResolver, testImpactTaskProvider)
        }
    }

    private fun addTestImpactTask(
        target: Project,
        extension: TestImpactExtension,
        graphResolver: ProjectGraphResolver,
    ): TaskProvider<TestImpactTask> {
        return target.tasks.register("testImpact", TestImpactTask::class.java) {
            group = "verification"

            if (!isWrapperTask(target) && !checkIsGradleStartedForTestImpact(target)) {
                throw IllegalStateException("Currently :testImpact allowed to run only as a gradle start task: ${target.gradle.startParameter.taskNames}")
            }

            if (!graphResolver.isCompleted) {
                throw IllegalStateException("Unable to use task before projects evaluation completed")
            }

            changesSource = extension.changesSource
            impactGraph = graphResolver.getProjectsGraph()
        }
    }

    private fun evaluateProjectsGraph(
        target: Project,
        extension: TestImpactExtension,
        graphResolver: ProjectGraphResolver,
    ) {
        target.afterEvaluate {
            graphResolver.markChangedProjects(extension.changesSource.getChangedFiles())
        }

        iterateOverProjects(target) { project ->
            project.afterEvaluate {
                graphResolver.onProjectEvaluated(project)
            }
        }
    }

    private fun buildTestImpactTasksGraph(
        target: Project,
        extension: TestImpactExtension,
        graphResolver: ProjectGraphResolver,
        taskProvider: TaskProvider<TestImpactTask>,
    ) {
        val testImpactTask = taskProvider.get()

        iterateOverProjects(target) { project ->
            if (
                extension.unchangedProjectTestStrategy == UnchangedProjectTestStrategy.SKIP_COMPILE &&
                !graphResolver.hasChanges(project)
            ) {
                return@iterateOverProjects
            }

            extension.testTasksNames.forEach { testTask ->
                project.tasks.findByName(testTask)?.apply {
                    testImpactTask.finalizedBy("${project.name}:$testTask")

                    setOnlyIf {
                        !target.gradle.taskGraph.hasTask(testImpactTask) ||
                                testImpactTask.hasChanges(project.name)
                    }
                    doFirst {
                        // TODO: setup tests filter
                    }
                }
            }
        }
    }

    private fun iterateOverProjects(rootProject: Project, action: (Project) -> Unit) {
        val projectsQueue = ArrayDeque<Project>()
        projectsQueue.add(rootProject)
        while (projectsQueue.isNotEmpty()) {
            val project = projectsQueue.removeFirst()
            action.invoke(project)

            project.childProjects.values.forEach(projectsQueue::add)
        }
    }

    private fun checkIsGradleStartedForTestImpact(target: Project): Boolean {
        return target.gradle.startParameter.taskNames.any { taskName ->
            taskName == "testImpact" && target === target.rootProject ||
                    taskName == "${target.name}:testImpact"
        }
    }

    private fun isWrapperTask(target: Project): Boolean {
        return target.gradle.startParameter.taskNames == listOf("wrapper")
    }
}
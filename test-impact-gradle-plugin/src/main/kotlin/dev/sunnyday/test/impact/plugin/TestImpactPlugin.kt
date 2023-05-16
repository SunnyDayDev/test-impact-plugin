package dev.sunnyday.test.impact.plugin

import dev.sunnyday.test.impact.plugin.extension.TestImpactExtension
import dev.sunnyday.test.impact.plugin.domain.graph.ProjectGraphResolver
import dev.sunnyday.test.impact.plugin.task.testimpact.TestImpactTask
import dev.sunnyday.test.impact.plugin.task.testimpact.TestImpactTaskOutput
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import java.io.File

class TestImpactPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        PluginApplier(target).apply()
    }

    private class PluginApplier(
        private val target: Project
    ) {

        private val extension = registerExtension()

        private val startParameterTaskNamesProvider = registerStartParameterTaskNamesProvider()
        private val graphResolverProvider = registerGraphResolverProvider()
        private val testImpactTaskOutputProvider = registerTestImpactTaskOutputProvider()
        private val testImpactTaskProvider = registerTestImpactTask()

        fun apply() {
            if (!isGradleStartedForTestImpact()) {
                return
            }

            target.gradle.projectsEvaluated {
                buildGraph()
                buildTestImpactTasksGraph()
            }
        }

        private fun registerExtension(): TestImpactExtension {
            return target.extensions.create("testImpact", TestImpactExtension::class.java)
        }

        private fun registerGraphResolverProvider(): Provider<ProjectGraphResolver> {
            val graphResolver = ProjectGraphResolver(target)
            return target.provider { graphResolver }
        }

        private fun registerStartParameterTaskNamesProvider(): Provider<List<String>> {
            return target.provider { target.gradle.startParameter.taskNames }
        }

        private fun registerTestImpactTask(): TaskProvider<TestImpactTask> {
            return target.tasks.register("testImpact", TestImpactTask::class.java) {
                group = "verification"

                if (!(isWrapperTask() || isGradleStartedForTestImpact())) {
                    throw IllegalStateException(
                        """
                            Currently :testImpact allowed to run only as a gradle start task.
                            Current start tasks: ${target.gradle.startParameter.taskNames}"
                        """.trimIndent()
                    )
                }

                changesSource = extension.changesSource
                inputImpactGraph = graphResolverProvider.get().getProjectsGraph()
                taskOutput = testImpactTaskOutputProvider.get()
            }
        }

        private fun registerTestImpactTaskOutputProvider(): Provider<TestImpactTaskOutput> {
            return target.provider {
                TestImpactTaskOutput(getTestImpactOutputFile())
            }
        }

        private fun buildGraph() {
            val graphResolver = graphResolverProvider.get()

            iterateOverProjects(graphResolver::onProjectEvaluated)
            graphResolver.markGraphCompleted()
        }

        private fun buildTestImpactTasksGraph() {
            iterateOverProjects { project ->
                val projectTestTasks = extension.testTaskNameProvider.getTestTasks(project)
                projectTestTasks.forEach { testTask ->
                    project.tasks.findByName(testTask)
                        ?.let(::setupTestTaskDependency)
                }
            }
        }

        private fun setupTestTaskDependency(testTask: Task) {
            val testImpactTask = testImpactTaskProvider.get()

            testTask.apply {
                dependsOn(testImpactTask.path)

                val testImpactTaskOutputProvider = testImpactTaskOutputProvider
                val testProjectPath = testTask.project.path
                setOnlyIf {
                    val testTaskOutputGraph = testImpactTaskOutputProvider.get()
                        .readImpactGraph()
                    testTaskOutputGraph.getProjectByProjectPath(testProjectPath).hasChanges
                }

                doFirst {
                    // TODO: setup tests filter
                }
            }

            val startTasks = startParameterTaskNamesProvider.get()
            if (startTasks.last() == getTestImpactStartTaskName()) {
                testImpactTask.finalizedBy(testTask.path)
            }
        }

        private fun iterateOverProjects(action: (Project) -> Unit) {
            val projectsQueue = ArrayDeque<Project>()
            projectsQueue.add(target)
            while (projectsQueue.isNotEmpty()) {
                val project = projectsQueue.removeFirst()
                action.invoke(project)

                project.childProjects.values.forEach(projectsQueue::add)
            }
        }

        private fun isGradleStartedForTestImpact(): Boolean {
            return startParameterTaskNamesProvider.get().contains(getTestImpactStartTaskName())
        }

        private fun getTestImpactStartTaskName(): String {
            return if (target.rootProject === target) "testImpact" else "${target.path}:testImpact"
        }

        private fun isWrapperTask(): Boolean {
            return startParameterTaskNamesProvider.get() == listOf("wrapper")
        }

        private fun getTestImpactOutputFile(): File {
            return File(target.buildDir, "tmp/testImpact/output.txt")
        }
    }
}
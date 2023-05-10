package dev.sunnyday.test.impact.plugin

import dev.sunnyday.test.impact.plugin.config.TestImpactConfiguration
import dev.sunnyday.test.impact.plugin.config.UnchangedProjectTestStrategy
import org.gradle.api.Plugin
import org.gradle.api.Project

class TestImpactPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create("testImpact", TestImpactConfiguration::class.java)

        val testImpactTaskProvider = target.tasks.register("testImpact", TestImpactTask::class.java) {
            group = "verification"

            changesSource = extension.changesSource
        }

        if (!checkIsGradleStartedForTestImpact(target)) {
            return
        }

        val projectsQueue = ArrayDeque<Project>()
        projectsQueue.add(target)
        while (projectsQueue.isNotEmpty()) {
            val project = projectsQueue.removeFirst()
            project.afterEvaluate {
                val testImpactTask = testImpactTaskProvider.get()

                testImpactTask.onProjectEvaluated(project)

                if (
                    extension.unchangedProjectTestStrategy == UnchangedProjectTestStrategy.SKIP_COMPILE &&
                    !testImpactTask.hasChanges(project)
                ) {
                    return@afterEvaluate
                }

                extension.testTasksNames.forEach { testTask ->
                    project.tasks.findByName(testTask)?.apply {
                        testImpactTask.finalizedBy("${project.name}:$testTask")

                        setOnlyIf { !testImpactTask.isRunned || testImpactTask.hasChanges(project) }
                        doFirst {
                            // TODO: setup tests filter
                        }
                    }
                }
            }

            project.childProjects.values.forEach(projectsQueue::add)
        }
    }

    private fun checkIsGradleStartedForTestImpact(target: Project): Boolean {
        return target.gradle.startParameter.taskNames.any { taskName ->
            taskName == "testImpact" && target === target.rootProject ||
                    taskName == "${target.name}:testImpact"
        }
    }
}
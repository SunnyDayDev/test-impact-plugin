package dev.sunnyday.test.impact.plugin

import dev.sunnyday.test.impact.plugin.config.ChangesSource
import dev.sunnyday.test.impact.plugin.graph.ImpactProject
import dev.sunnyday.test.impact.plugin.graph.ImpactProjectGraph
import dev.sunnyday.test.impact.plugin.graph.ProjectGraphResolver
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

open class TestImpactTask : DefaultTask() {

    @get:Nested
    lateinit var changesSource: ChangesSource

    @get:Internal
    var isRunned: Boolean = false
        private set

    private val graph: ImpactProjectGraph by lazy {
        ProjectGraphResolver(project)
            .getProjectsGraph()
            .also(::markChangedProjects)
    }

    @TaskAction
    fun run() {

        isRunned = true
    }

    private fun markChangedProjects(graph: ImpactProjectGraph) {
        val changedProjects = changesSource.getChangedFiles()
            .mapNotNullTo(mutableSetOf(), graph::getProjectByRelativePath)
        // TODO: build tasks graph from roots
        val changedProjectsRoots = changedProjects.toMutableSet()

        val queue = ArrayDeque<ImpactProject>()
        queue.addAll(changedProjects)

        while (queue.isNotEmpty()) {
            val project = queue.removeLast()

            project.hasChanges = true

            project.dependentProjects.forEach { dependentProject ->
                changedProjectsRoots.remove(dependentProject)

                if (!dependentProject.hasChanges && changedProjects.add(dependentProject)) {
                    queue.add(dependentProject)
                }
            }
        }
    }

    fun hasChanges(target: Project): Boolean {
        return graph.getProjectByName(target.name).hasChanges
    }
}
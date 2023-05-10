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

    private val graphResolver = ProjectGraphResolver(project)

    private val graph: ImpactProjectGraph by lazy {
        graphResolver.getProjectsGraph()
            .also(::markChangedProjects)
    }

    private fun markChangedProjects(graph: ImpactProjectGraph) {
        val changedProjects = changesSource.getChangedFiles()
            .mapNotNullTo(mutableSetOf(), graph::getProjectByRelativePath)

        val queue = ArrayDeque<ImpactProject>()
        queue.addAll(changedProjects)

        while (queue.isNotEmpty()) {
            val project = queue.removeLast()

            project.hasChanges = true

            project.dependentProjects.forEach { dependentProject ->
                if (!dependentProject.hasChanges && changedProjects.add(dependentProject)) {
                    queue.add(dependentProject)
                }
            }
        }
    }

    fun onProjectEvaluated(project: Project) {
        graphResolver.onProjectEvaluated(project)
    }

    fun hasChanges(target: Project): Boolean {
        return graph.getProjectByName(target.name).hasChanges
    }

    @TaskAction
    fun run() {
        isRunned = true
    }
}
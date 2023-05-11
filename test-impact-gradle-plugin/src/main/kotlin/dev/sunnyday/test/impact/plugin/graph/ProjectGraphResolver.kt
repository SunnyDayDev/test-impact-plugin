package dev.sunnyday.test.impact.plugin.graph

import dev.sunnyday.test.impact.plugin.model.ImpactProject
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.withType

internal class ProjectGraphResolver(
    private val rootProject: Project,
) {

    private val projects = getProjectsMap()
    private val roots = projects.values.toMutableSet()
    private val graph = projects.values.associateWith { mutableListOf<ImpactProject>() }
    private val pathTrie = buildProjectsPathTrie(projects.values)

    var isCompleted: Boolean = false
        private set

    private fun getProjectsMap(): Map<String, ImpactProject> {
        return buildMap {
            val projectsQueue = ArrayDeque<Project>()
            projectsQueue.add(rootProject)

            while (projectsQueue.isNotEmpty()) {
                val project = projectsQueue.removeFirst()

                project.childProjects.values.forEach(projectsQueue::add)

                this[project.name] = ImpactProject(
                    name = project.name,
                    path = project.relativePathToRoot,
                )
            }
        }
    }

    fun markChangedProjects(changedFilesPaths: List<String>) {
        changedFilesPaths
            .asSequence()
            .mapNotNull(pathTrie::getProjectByRelativePath)
            .forEach { project -> project.hasChanges = true }
    }

    fun onProjectEvaluated(project: Project) {
        val impactProject = projects.getValue(project.name)

        var isRemovedFromRoots = false

        project.configurations
            .filter { it.name.endsWith("implementation", ignoreCase = true) }
            .flatMap { configuration ->
                configuration.dependencies.withType<ProjectDependency>()
                    .map { dependency -> configuration to dependency }
            }
            .forEach { (configuration, dependency) ->
                if (!isRemovedFromRoots && !configuration.name.contains("test", ignoreCase = true)) {
                    roots.remove(impactProject)
                    isRemovedFromRoots = true
                }

                val dependencyNode = projects.getValue(dependency.dependencyProject.name)
                val dependencyList = graph.getValue(dependencyNode)
                dependencyList.add(impactProject)

                if (!impactProject.hasChanges && dependencyNode.hasChanges) {
                    impactProject.hasChanges = true
                }
            }
    }

    fun hasChanges(project: Project): Boolean {
        return pathTrie.getProjectByRelativePath(project.relativePathToRoot)?.hasChanges ?: false
    }

    fun getProjectsGraph(): ImpactProjectGraph {
        return ImpactProjectGraph(
            roots = roots.toSet(),
            projects = projects.toMap(),
            graph = graph.toMap(),
            pathTrie = pathTrie,
        )
    }

    fun markGraphCompleted() {
        isCompleted = true
    }

    private fun buildProjectsPathTrie(projects: Iterable<ImpactProject>): ProjectPathTrie {
        val trie = ProjectPathTrie()
        projects.forEach(trie::add)
        return trie
    }

    private val Project.relativePathToRoot: String
        get() = projectDir.relativeTo(rootDir).path
}
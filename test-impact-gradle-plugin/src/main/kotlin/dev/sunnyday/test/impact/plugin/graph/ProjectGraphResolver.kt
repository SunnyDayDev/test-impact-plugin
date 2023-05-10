package dev.sunnyday.test.impact.plugin.graph

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.withType

internal class ProjectGraphResolver(
    private val rootProject: Project,
) {

    private val projects by lazy { getProjectsMap() }
    private val roots by lazy { projects.values.toMutableSet() }

    fun getProjectsGraph(): ImpactProjectGraph {
        val pathTrie = buildProjectsPathTrie(projects.values)

        return ImpactProjectGraph(roots, projects, pathTrie)
    }

    private fun getProjectsMap(): Map<String, ImpactProject> {
        return buildMap {
            val projectsQueue = ArrayDeque<Project>()
            projectsQueue.add(rootProject)

            while (projectsQueue.isNotEmpty()) {
                val project = projectsQueue.removeFirst()

                project.childProjects.values.forEach(projectsQueue::add)

                this[project.name] = ImpactProject(project)
            }
        }
    }

    fun onProjectEvaluated(project: Project) {
        val impactProject = projects[project.name] ?: return

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
                val dependencyList = dependencyNode.dependentProjects
                dependencyList.add(impactProject)

                if (dependencyNode.hasChanges) {
                    impactProject.hasChanges = true
                }
            }
    }

    private fun buildProjectsPathTrie(projects: Iterable<ImpactProject>): ProjectPathTrie {
        val trie = ProjectPathTrie()
        projects.forEach(trie::add)
        return trie
    }
}
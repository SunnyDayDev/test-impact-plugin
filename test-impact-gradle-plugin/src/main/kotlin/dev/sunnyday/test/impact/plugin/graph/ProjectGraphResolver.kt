package dev.sunnyday.test.impact.plugin.graph

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.withType

internal class ProjectGraphResolver(
    private val rootProject: Project,
) {

    fun getProjectsGraph(): ImpactProjectGraph {
        val projects = getProjectsMap()

        val roots = buildProjectsGraph(projects.values)
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

    private fun buildProjectsGraph(projects: Iterable<ImpactProject>): List<ImpactProject> {
        val projectsMap = projects.associateBy { (project) -> project }
        val roots = projects.toMutableSet()

        projects.forEach { dependentNode ->
            var isRemovedFromRoots = false

            dependentNode.project.configurations
                .filter { it.name.endsWith("implementation", ignoreCase = true) }
                .flatMap { configuration ->
                    configuration.dependencies.withType<ProjectDependency>()
                        .map { dependency -> configuration to dependency }
                }
                .forEach { (configuration, dependency) ->
                    if (!isRemovedFromRoots && !configuration.name.contains("test", ignoreCase = true)) {
                        roots.remove(dependentNode)
                        isRemovedFromRoots = true
                    }

                    val dependencyNode = projectsMap.getValue(dependency.dependencyProject)
                    val dependencyList = dependencyNode.dependentProjects
                    dependencyList.add(dependentNode)
                }
        }

        return roots.toList()
    }

    private fun buildProjectsPathTrie(projects: Iterable<ImpactProject>): ProjectPathTrie {
        val trie = ProjectPathTrie()
        projects.forEach(trie::add)
        return trie
    }
}
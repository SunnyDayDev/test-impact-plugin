package dev.sunnyday.test.impact.plugin.domain.graph

import dev.sunnyday.test.impact.plugin.domain.model.ImpactProject
import dev.sunnyday.test.impact.plugin.domain.model.ProjectPath
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.kotlin.dsl.withType

internal class ProjectGraphResolver(
    rootProject: Project,
) {

    private val projects = getProjectsMap(rootProject)
    private val roots = projects.values.mapTo(hashSetOf(), ImpactProject::path)
    private val graph = projects.values.associateTo(hashMapOf()) { it.path to mutableListOf<ProjectPath>() }
    private val pathTrie = buildProjectsPathTrie(projects.values)

    var isCompleted: Boolean = false
        private set

    private fun getProjectsMap(rootProject: Project): Map<String, ImpactProject> {
        return hashMapOf<String, ImpactProject>().apply {
            val projectsQueue = ArrayDeque<Project>()
            projectsQueue.add(rootProject)

            while (projectsQueue.isNotEmpty()) {
                val project = projectsQueue.removeFirst()

                project.childProjects.values.forEach(projectsQueue::add)

                this[project.path] = ImpactProject(
                    path = project.path,
                    dirPath = project.relativePathToRoot,
                )
            }
        }
    }

    fun onProjectEvaluated(project: Project) {
        val impactProject = projects.getValue(project.path)

        var isRemovedFromRoots = false

        project.configurations
            .filter { it.name.endsWith("implementation", ignoreCase = true) }
            .flatMap { configuration ->
                configuration.dependencies.withType<ProjectDependency>()
                    .map { dependency -> configuration to dependency }
            }
            .forEach { (configuration, dependency) ->
                if (!isRemovedFromRoots && !configuration.name.contains("test", ignoreCase = true)) {
                    roots.remove(impactProject.path)
                    isRemovedFromRoots = true
                }

                val dependencyNode = projects.getValue(dependency.dependencyProject.path)
                val dependencyList = graph.getValue(dependencyNode.path)
                dependencyList.add(impactProject.path)
            }
    }

    fun getProjectsGraph(): ImpactProjectGraph {
        return ImpactProjectGraph(
            roots = roots,
            projects = projects,
            graph = graph,
            filePathTrie = pathTrie,
        )
    }

    fun markGraphCompleted() {
        isCompleted = true
    }

    private fun buildProjectsPathTrie(projects: Iterable<ImpactProject>): ProjectFilePathTrie {
        val trie = ProjectFilePathTrie()
        projects.forEach(trie::add)
        return trie
    }

    private val Project.relativePathToRoot: String
        get() = projectDir.relativeTo(rootDir).path
}
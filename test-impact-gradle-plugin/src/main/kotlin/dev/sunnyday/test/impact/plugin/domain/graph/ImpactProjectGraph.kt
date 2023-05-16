package dev.sunnyday.test.impact.plugin.domain.graph

import dev.sunnyday.test.impact.plugin.domain.model.ImpactProject
import dev.sunnyday.test.impact.plugin.domain.model.ProjectPath
import java.io.Serializable

internal class ImpactProjectGraph(
    private val projects: Map<ProjectPath, ImpactProject>,
    private val roots: Set<ProjectPath>,
    private val graph: Map<ProjectPath, List<ProjectPath>>,
    private val filePathTrie: ProjectFilePathTrie,
) : Serializable {

    fun getProjectByProjectPath(name: String): ImpactProject {
        return projects.getValue(name)
    }

    fun getProjectByRelativePath(filePath: String): ImpactProject? {
        return filePathTrie.getProjectByRelativePath(filePath)
            ?.let(projects::get)
    }

    fun getDependentProjects(project: ImpactProject): List<ImpactProject> {
        return graph.getOrDefault(project.path, emptyList())
            .mapNotNull(projects::get)
    }

    fun clone(): ImpactProjectGraph {
        val clonedProjects = projects.mapValuesTo(hashMapOf()) { (_, project) ->
            project.copy()
        }

        val clonedRoots = roots.toHashSet()
        val clonedGraph = graph.toMap(hashMapOf())
        val clonedPathTrie = filePathTrie.clone()

        return ImpactProjectGraph(
            roots = clonedRoots,
            projects = clonedProjects,
            graph = clonedGraph,
            filePathTrie = clonedPathTrie,
        )
    }

    override fun toString(): String {
        return "ImpactProjectGraph($graph)"
    }
}
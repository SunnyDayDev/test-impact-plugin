package dev.sunnyday.test.impact.plugin.graph

import dev.sunnyday.test.impact.plugin.model.ImpactProject
import java.io.Serializable

internal class ImpactProjectGraph(
    private val roots: Set<ImpactProject>,
    private val projects: Map<String, ImpactProject>,
    private val graph: Map<ImpactProject, List<ImpactProject>>,
    private val pathTrie: ProjectPathTrie,
): Serializable {

    fun getProjectByName(name: String): ImpactProject {
        return projects.getValue(name)
    }

    fun getProjectByRelativePath(filePath: String): ImpactProject? {
        return pathTrie.getProjectByRelativePath(filePath)
    }
}
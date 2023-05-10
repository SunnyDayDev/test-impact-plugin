package dev.sunnyday.test.impact.plugin.graph

internal class ImpactProjectGraph(
    val roots: Set<ImpactProject>,
    private val projects: Map<String, ImpactProject>,
    private val pathTrie: ProjectPathTrie,
) {

    fun getProjectByName(name: String): ImpactProject {
        return projects.getValue(name)
    }

    fun getProjectByRelativePath(filePath: String): ImpactProject? {
        return pathTrie.getProjectByRelativePath(filePath)
    }
}
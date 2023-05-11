package dev.sunnyday.test.impact.plugin.graph

import dev.sunnyday.test.impact.plugin.model.ImpactProject
import java.io.Serializable

internal class ProjectPathTrie : Serializable {

    private val root = PathNode()

    fun add(impactProject: ImpactProject) {
        impactProject.path
            .split("/")
            .fold(root, PathNode::add)
            .markProject(impactProject)
    }

    fun getProjectByRelativePath(filePath: String): ImpactProject? {
        var lastProject: ImpactProject? = null

        filePath.split("/").asSequence()
            .runningFold(root as PathNode?) { node, partPath -> node?.get(partPath)}
            .takeWhile { it != null }
            .mapNotNull { it?.project }
            .forEach { lastProject = it}

        return lastProject
    }


    private class PathNode : Serializable {
        private val children = mutableMapOf<String, PathNode>()

        var project: ImpactProject? = null
            private set

        fun add(pathPart: String): PathNode {
            return children.getOrPut(pathPart, ::PathNode)
        }

        fun markProject(project: ImpactProject) {
            this.project = project
        }

        fun get(pathPart: String): PathNode? {
            return children[pathPart]
        }
    }
}
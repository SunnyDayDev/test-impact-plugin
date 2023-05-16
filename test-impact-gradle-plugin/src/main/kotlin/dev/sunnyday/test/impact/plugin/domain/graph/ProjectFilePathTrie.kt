package dev.sunnyday.test.impact.plugin.domain.graph

import dev.sunnyday.test.impact.plugin.domain.model.ImpactProject
import dev.sunnyday.test.impact.plugin.domain.model.ProjectPath
import java.io.Serializable

internal class ProjectFilePathTrie private constructor(
    private val root: PathNode,
) : Serializable {

    constructor() : this(PathNode())

    fun add(impactProject: ImpactProject) {
        impactProject.dirPath
            .split("/")
            .fold(root, PathNode::add)
            .markProject(impactProject.path)
    }

    fun getProjectByRelativePath(filePath: String): ProjectPath? {
        var lastProject: ProjectPath? = null

        filePath.split("/").asSequence()
            .runningFold(root as PathNode?) { node, partPath -> node?.get(partPath) }
            .takeWhile { it != null }
            .mapNotNull { it?.project }
            .forEach { lastProject = it }

        return lastProject
    }

    fun clone(): ProjectFilePathTrie {
        return ProjectFilePathTrie(root.clone())
    }

    private class PathNode : Serializable {

        private val children = hashMapOf<String, PathNode>()

        var project: ProjectPath? = null
            private set

        fun add(pathPart: String): PathNode {
            return children.getOrPut(pathPart, ProjectFilePathTrie::PathNode)
        }

        fun markProject(project: ProjectPath) {
            this.project = project
        }

        fun get(pathPart: String): PathNode? {
            return children[pathPart]
        }

        fun clone(): PathNode {
            val origin = this
            return PathNode().apply {
                children.putAll(origin.children)
                project = origin.project
            }
        }
    }
}
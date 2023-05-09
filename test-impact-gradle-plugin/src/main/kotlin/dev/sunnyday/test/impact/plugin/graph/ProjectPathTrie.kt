package dev.sunnyday.test.impact.plugin.graph

internal class ProjectPathTrie {

    private val root = Node()

    fun add(impactProject: ImpactProject) {
        val project = impactProject.project

        project.projectDir.relativeTo(project.rootDir).path
            .split("/")
            .fold(root, Node::add)
            .markProject(impactProject)
    }

    fun getProjectByRelativePath(filePath: String): ImpactProject? {
        var lastProject: ImpactProject? = null

        filePath.split("/").asSequence()
            .runningFold(root as Node?) { node, partPath -> node?.get(partPath)}
            .takeWhile { it != null }
            .mapNotNull { it?.project }
            .forEach { lastProject = it}

        return lastProject
    }


    private class Node {
        private val children = mutableMapOf<String, Node>()

        var project: ImpactProject? = null
            private set

        fun add(pathPart: String): Node {
            return children.getOrPut(pathPart, ::Node)
        }

        fun markProject(project: ImpactProject) {
            this.project = project
        }

        fun get(pathPart: String): Node? {
            return children[pathPart]
        }
    }
}
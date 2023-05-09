package dev.sunnyday.test.impact.plugin.graph

import org.gradle.api.Project

internal class ImpactProject(
    val project: Project,
) {

    val dependentProjects = mutableListOf<ImpactProject>()

    var hasChanges: Boolean = false

    // val hasTests: Boolean
    //     get() = project.tasks.names.contains("test") && project.file("src/test").exists()

    operator fun component1() = project

    override fun equals(other: Any?): Boolean {
        return (other as? ImpactProject)?.project?.name == project.name
    }

    override fun hashCode(): Int {
        return project.name.hashCode()
    }

    override fun toString(): String {
        return project.name
    }
}
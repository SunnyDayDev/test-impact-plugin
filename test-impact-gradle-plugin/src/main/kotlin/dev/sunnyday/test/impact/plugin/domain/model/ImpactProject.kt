package dev.sunnyday.test.impact.plugin.domain.model

import java.io.Serializable

internal data class ImpactProject(
    val path: ProjectPath,
    val dirPath: String,
    var hasChanges: Boolean = false
) : Serializable {

    override fun toString(): String {
        return path
    }
}
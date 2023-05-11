package dev.sunnyday.test.impact.plugin.model

import java.io.Serializable

internal data class ImpactProject(
    val name: String,
    val path: String,
) : Serializable {

    var hasChanges: Boolean = false

    override fun toString(): String {
        return name
    }
}
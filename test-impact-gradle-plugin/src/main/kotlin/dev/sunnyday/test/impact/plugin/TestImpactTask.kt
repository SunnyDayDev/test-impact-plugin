package dev.sunnyday.test.impact.plugin

import dev.sunnyday.test.impact.plugin.config.ChangesSource
import dev.sunnyday.test.impact.plugin.graph.ImpactProjectGraph
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.TaskAction

open class TestImpactTask : DefaultTask() {

    @get:Nested
    lateinit var changesSource: ChangesSource


    @get:Input
    internal lateinit var impactGraph: ImpactProjectGraph

    fun hasChanges(projectName: String): Boolean {
        return impactGraph.getProjectByName(projectName).hasChanges
    }

    @TaskAction
    fun run() {
        // TODO: parse packages and collect necessary tests
    }
}
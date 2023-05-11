package dev.sunnyday.test.impact.plugin.config

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Nested

open class TestImpactExtension {

    @get:Input
    var testTasksNames: List<String> = listOf("test")

    @get:Input
    var unchangedProjectTestStrategy: UnchangedProjectTestStrategy = UnchangedProjectTestStrategy.SKIP_TEST

    @get:Nested
    var changesSource: ChangesSource = ChangesSource(::emptyList)
}
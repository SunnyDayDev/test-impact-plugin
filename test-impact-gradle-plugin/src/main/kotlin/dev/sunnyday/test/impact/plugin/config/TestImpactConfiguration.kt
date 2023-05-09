package dev.sunnyday.test.impact.plugin.config

import org.gradle.api.tasks.Input

open class TestImpactConfiguration {

    @Input
    var testTasksNames: List<String> = listOf("test")

    @Input
    var unchangedProjectTestStrategy: UnchangedProjectTestStrategy = UnchangedProjectTestStrategy.SKIP_TEST

    var changesSource: ChangesSource = ChangesSource(::emptyList)
}
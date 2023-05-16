package dev.sunnyday.test.impact.plugin.extension

import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested

open class TestImpactExtension {

    @get:Internal
    var testTaskNameProvider: TestTaskNameProvider = TestTaskNameProvider { listOf("test") }

    @get:Nested
    var changesSource: ChangesSource = ChangesSource(::emptyList)
}
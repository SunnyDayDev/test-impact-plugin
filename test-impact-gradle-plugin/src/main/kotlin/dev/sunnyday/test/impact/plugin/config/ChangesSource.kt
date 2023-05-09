package dev.sunnyday.test.impact.plugin.config

import org.gradle.api.tasks.Input

fun interface ChangesSource {

    @Input
    fun getChangedFiles(): List<String>
}
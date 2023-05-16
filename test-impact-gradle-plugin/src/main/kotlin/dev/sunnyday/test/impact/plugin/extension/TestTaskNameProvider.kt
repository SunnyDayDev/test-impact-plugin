package dev.sunnyday.test.impact.plugin.extension

import org.gradle.api.Project

fun interface TestTaskNameProvider {

    fun getTestTasks(project: Project): List<String>
}
[![](https://jitpack.io/v/dev.sunnyday/test-impact-plugin.svg)](https://jitpack.io/#dev.sunnyday/test-impact-plugin)

# test-impact-plugin
Plugin for running tests based on impact analysis of changes.

# Integration
```kotlin
// settings.gradle.kts

pluginManagement {
    repositories {
        maven { url = uri("https://jitpack.io" )}
        // ...
    }
}
```

```kotlin
// build.gradle.kts

plugins {
    id("dev.sunnyday.test-impact-plugin")
}

testImpact {
    testTasksNames = listOf("testDebugUnitTest")
    unchangedProjectTestStrategy = UnchangedProjectTestStrategy.SKIP_TEST
    
    // Later, predefined sources will be added here, such as git diff
    changesSource = ChangesSource {
        listOf(
            "subproject/src/main/kotlin/some/package/SomeClass.kt",
        )
    }
}
```

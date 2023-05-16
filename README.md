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
    // Later, predefined providers will be added here, such as androidTests("buildType"...)
    testTaskNameProvider = TestTaskNameProvider {
        listOf("test")
    }
    
    // Later, predefined sources will be added here, such as git diff
    changesSource = ChangesSource {
        listOf(
            "subproject/src/main/kotlin/some/package/SomeClass.kt",
        )
    }
}
```

# Execution
Currently testImpact requires to be a start task to run properly, otherwise it will be ignored.
To run all the affected tests, simply run the task, and when it completes, the tasks specified in `testTaskNameProvider` will run.
```bash
./gradlew testImpact
```

To run the specified tests if they are affected, run the tests after the testImpact task.
```bash
./gradlew testImpact :subproject:test
```

plugins {
    alias(libs.plugins.kotlin.dsl)
    `maven-publish`
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(libs.versions.jdkVersion.get().toInt())
}

gradlePlugin {
    plugins {
        register("test-impact") {
            description = ""
            displayName = "Test impact"
            id = "dev.sunnyday.test-impact-plugin"
            implementationClass = "dev.sunnyday.test.impact.plugin.TestImpactPlugin"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            group = "dev.sunnyday.test-impact"
            artifactId = "test-impact-gradle-plugin"
            version = "0.1.0"

            from(components["kotlin"])
        }
    }
}
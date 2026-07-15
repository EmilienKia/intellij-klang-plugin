plugins {
    id("java")
    alias(libs.plugins.kotlin)
    alias(libs.plugins.intellijPlatform)
}

group = "com.github.emilienkia.klang"
version = "1.0.0-SNAPSHOT"

// Set the JVM language level used to build the project.
kotlin {
    jvmToolchain(21)
}

repositories {
    mavenLocal()
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.JUnit5)

        // Add plugin dependencies for compilation here, for example:
        // bundledPlugin("com.intellij.java")

        // Spellchecker support: intellij.spellchecker lives in lib/modules/ (platform module,
        // not a bundled plugin), so bundledModule is required to put it on the compile classpath.
        // No <depends> entry in plugin.xml is needed — platform modules are always available.
        bundledModule("intellij.spellchecker")
        // The concrete SpellCheckingInspection implementation is GrazieSpellCheckingInspection
        // in the 'tanvd.grazi' bundled plugin. Adding this makes it available in tests so
        // fixture.enableInspections(GrazieSpellCheckingInspection.class) resolves correctly.
        bundledPlugin("tanvd.grazi")
    }

    // KDI library support: kdi-java reader + its Jackson transitive dependencies.
    // Marked as implementation so they are bundled into the plugin distribution.
    implementation("com.github.emilienkia.klang:klang-kdi:0.1-SNAPSHOT")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.18.2")

    // Unit/PSI tests: JUnit 5 (Jupiter) + AssertJ. Jupiter must be >= 5.12 because the
    // IntelliJ JUnit5 test framework (TestFixtureExtension) calls APIs added in 5.12.
    testImplementation("org.junit.jupiter:junit-jupiter:5.12.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.12.2")
    testImplementation("org.assertj:assertj-core:3.27.3")
    // The IntelliJ platform test framework's JUnit5 session listener references
    // junit.framework.TestCase, so JUnit 4 must be present on the test classpath.
    testRuntimeOnly("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
}

sourceSets {
    main {
        java {
            srcDirs("src/main/gen")
        }
    }
}

tasks {
    wrapper {
        gradleVersion = providers.gradleProperty("gradleVersion").get()
    }

    test {
        useJUnitPlatform()
    }
}

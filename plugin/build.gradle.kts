plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.0"

    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.14.0"
}

group = "com.github.redgreencoding"
version = "0.0.1"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("net.sourceforge.plantuml:plantuml:1.2021.10")

    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    testImplementation("io.strikt:strikt-core:0.31.0")
    testImplementation("io.strikt:strikt-jvm:0.31.0")
}

gradlePlugin {
    // Define the plugin
    val plantuml by plugins.creating {
        id = "com.github.redgreencoding.plantuml"
        displayName = "Gradle PlantUML Plugin"
        description = "A plugin to convert PlantUML .puml files to one of the supported output formats"
        implementationClass = "com.github.redgreencoding.plantuml.PlantumlGradlePlugin"
    }
}

pluginBundle {
    website = "https://github.com/red-green-coding/plantuml-gradle-plugin"
    vcsUrl = "https://github.com/red-green-coding/plantuml-gradle-plugin.git"
    tags = listOf("plantuml", "puml", "svg")
    mavenCoordinates {
        group = project.group.toString()
        artifactId = base.archivesBaseName
    }
    plugins {
        named("plantuml") {
            displayName = "Gradle PlantUML Plugin"
            description = "Gradle PlantUML Plugin"
        }
    }
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations["functionalTestImplementation"].extendsFrom(configurations["testImplementation"])

// Add a task to run the functional tests
val functionalTest by tasks.registering(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

tasks.check {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.withType(Test::class.java) {
    testLogging.showStandardStreams = true
}

tasks {
    validatePlugins {
        enableStricterValidation.set(true)
        failOnWarning.set(true)
    }
    jar {
        from(sourceSets.main.map { it.allSource })
        manifest.attributes.apply {
            put("Implementation-Title", "Gradle Kotlin DSL (${project.name})")
            put("Implementation-Version", archiveVersion.get())
        }
    }
}

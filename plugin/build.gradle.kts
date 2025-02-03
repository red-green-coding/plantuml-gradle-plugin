plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "2.1.10"

    id("com.gradle.plugin-publish") version "1.3.1"
}

group = "io.github.redgreencoding"
version = project.findProperty("projectVersion") ?: "0.0.3-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(gradleApi())

    implementation("net.sourceforge.plantuml:plantuml:1.2024.8")

    val kotestVersion = "5.9.1"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

gradlePlugin {
    website.set("https://github.com/red-green-coding/plantuml-gradle-plugin")
    vcsUrl.set("https://github.com/red-green-coding/plantuml-gradle-plugin.git")

    plugins {
        create("plantuml") {
            id = "io.github.redgreencoding.plantuml"
            displayName = "Gradle PlantUML Plugin"
            description = "A plugin to convert PlantUML .puml files to one of the supported output formats"
            implementationClass = "io.github.redgreencoding.plantuml.PlantumlGradlePlugin"
            tags.set(listOf("plantuml", "puml", "svg"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            artifactId = "plantuml-gradle-plugin"
        }
    }

    repositories {
        maven {
            name = "localPluginRepository"
            url = uri("../build/local-plugin-repository")
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
    useJUnitPlatform()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        allWarningsAsErrors = true

        incremental = true
        freeCompilerArgs = listOf(
            "-Xjsr305=strict"
        )
    }
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

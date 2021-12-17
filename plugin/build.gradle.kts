plugins {
    // Apply the Java Gradle plugin development plugin to add support for developing Gradle plugins
    `java-gradle-plugin`

    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.5.31"

    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.18.0"
}

group = "io.github.redgreencoding"
version = "0.1.1"

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

    implementation("net.sourceforge.plantuml:plantuml:1.2021.16")

    val kotestVersion = "4.6.3"
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
}

gradlePlugin {
    plugins {
        create("plantuml") {
            id = "io.github.redgreencoding.plantuml"
            displayName = "Gradle PlantUML Plugin"
            description = "A plugin to convert PlantUML .puml files to one of the supported output formats"
            implementationClass = "io.github.redgreencoding.plantuml.PlantumlGradlePlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/red-green-coding/plantuml-gradle-plugin"
    vcsUrl = "https://github.com/red-green-coding/plantuml-gradle-plugin.git"
    tags = listOf("plantuml", "puml", "svg")
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

        jvmTarget = "1.8"
        apiVersion = "1.5"
        languageVersion = "1.5"

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

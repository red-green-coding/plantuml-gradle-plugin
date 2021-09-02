package com.github.redgreencoding.plantuml

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import strikt.java.exists
import java.io.File
import kotlin.test.Test

/**
 * A simple functional test for the 'com.github.redgreencoding.plantuml.greeting' plugin.
 */
class PlantumlGradlePluginFunctionalTest {
    @Test fun `can run task with groovy dsl`() {
        // Setup the test build
        val projectDir = File("build/functionalTest/groovy")
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle").writeText("")
        projectDir.resolve("build.gradle").writeText("""
            plugins {
                id('redgreencoding.plantuml')
            }
            
            plantuml {
                    options {
                        outputDir = project.file("svg")
                    }

                    diagrams {
                        Hello
                    }
            }
        """)

        projectDir.resolve("Hello.puml").writeText("""
            @startuml

            Bob->Alice : Hello World!

            @enduml
        """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("--rerun-tasks", "plantumlAll")
        runner.withProjectDir(projectDir)
        val result = runner.build();

        expectThat(result.task(":plantumlAll")).isNotNull().get { outcome }.isEqualTo(TaskOutcome.SUCCESS)
        expectThat(result.task(":plantumlHello")).isNotNull().get { outcome }.isEqualTo(TaskOutcome.SUCCESS)
        expectThat(File(projectDir, "svg")).exists()
        val svg = File(projectDir, "svg/Hello.svg")
        expectThat(svg).exists()

        val content = svg.readText()

        println(content)
    }

    @Test fun `can run task with kotlin dsl`() {
        // Setup the test build
        val projectDir = File("build/functionalTest/kotlin")
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle.kts").writeText("")
        projectDir.resolve("build.gradle.kts").writeText("""
            plugins {
                id("redgreencoding.plantuml")
            }
            
            plantuml {
                    options {
                        outputDir = project.file("svg")
                    }

                    diagrams {
                        create("Hello")
                    }
            }
        """)

        projectDir.resolve("Hello.puml").writeText("""
            @startuml

            Bob->Alice : Hello World!

            @enduml
        """.trimIndent())

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("--rerun-tasks", "plantumlAll")
        runner.withProjectDir(projectDir)
        val result = runner.build();

        expectThat(result.task(":plantumlAll")).isNotNull().get { outcome }.isEqualTo(TaskOutcome.SUCCESS)
        expectThat(result.task(":plantumlHello")).isNotNull().get { outcome }.isEqualTo(TaskOutcome.SUCCESS)
        expectThat(File(projectDir, "svg")).exists()
        val svg = File(projectDir, "svg/Hello.svg")
        expectThat(svg).exists()

        val content = svg.readText()

        println(content)
    }
}

/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.github.redgreencoding.plantuml

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import strikt.api.expectThat
import strikt.assertions.isTrue
import kotlin.test.Test
import kotlin.test.assertTrue
import strikt.api.*
import strikt.assertions.*
import strikt.java.*

/**
 * A simple functional test for the 'com.github.redgreencoding.plantuml.greeting' plugin.
 */
class PlantumlGradlePluginFunctionalTest {
    @Test fun `can run task`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
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
        runner.withArguments("plantumlAll")
        runner.withProjectDir(projectDir)
        val result = runner.build();

        expectThat(File(projectDir, "svg")).exists()
        expectThat(File(projectDir, "svg/Hello.svg")).exists()
    }
}
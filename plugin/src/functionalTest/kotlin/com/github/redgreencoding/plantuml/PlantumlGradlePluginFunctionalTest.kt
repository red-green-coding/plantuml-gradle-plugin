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
        canGeneratePlantuml("build/functionalTest/groovy", "") {
            """
            plugins {
                id('com.github.redgreencoding.plantuml')
            }
            
            plantuml {
                    options {
                        outputDir = project.file("svg")
                    }

                    diagrams {
                        Hello
                        
                        Hello2 {
                            sourceFile = project.file("Hello.puml")
                        }
                    }
            }
        """
        }
    }

    @Test fun `can run task with kotlin dsl`() {
        canGeneratePlantuml("build/functionalTest/kotlin", ".kts") {
            """
            plugins {
                id("com.github.redgreencoding.plantuml")
            }
            
            plantuml {
                    options {
                        outputDir = project.file("svg")
                    }

                    diagrams {
                        create("Hello")
                        
                        create("Hello2") {
                            sourceFile = project.file("Hello.puml")
                        }
                    }
            }
        """
        }
    }

    private fun canGeneratePlantuml(directoryName: String, extension: String, buildFile: () -> String) {
        // Setup the test build
        val projectDir = File(directoryName)
        projectDir.mkdirs()
        projectDir.resolve("settings.gradle$extension").writeText("")
        projectDir.resolve("build.gradle$extension").writeText(buildFile())

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

        listOf(":plantumlAll", ":plantumlHello", ":plantumlHello2").forEach {
            expectThat(result.task(it)).isNotNull().get { outcome }.isEqualTo(TaskOutcome.SUCCESS)

        }

        listOf("Hello.svg", "Hello2.svg").forEach {
            val svg = File(projectDir, "svg/$it")
            expectThat(svg).exists()

            val content = svg.readText()

            println(content)
        }
    }
}

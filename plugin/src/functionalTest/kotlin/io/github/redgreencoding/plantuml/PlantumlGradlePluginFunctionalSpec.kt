package io.github.redgreencoding.plantuml

import io.kotest.core.spec.style.StringSpec
import io.kotest.core.spec.style.stringSpec
import io.kotest.matchers.file.shouldExist
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import java.net.URL
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.Schema
import javax.xml.validation.SchemaFactory
import javax.xml.validation.Validator

/**
 * A simple functional test for the 'io.github.redgreencoding.plantuml.greeting' plugin.
 */
class PlantumlGradlePluginFunctionalSpec : StringSpec({

    fun usingExtension() {
        val gradleDSL =
            """
            plugins {
                id('io.github.redgreencoding.plantuml')
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

        include(canConfigurePlantUmlPlugin("gradleExtension", "", gradleDSL))

        val kotlinDSL =
            """
            plugins {
                id("io.github.redgreencoding.plantuml")
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

        include(canConfigurePlantUmlPlugin("kotlinExtension", ".kts", kotlinDSL))
    }

    fun usingTasks() {

        val gradleDSL =
            """
            import io.github.redgreencoding.plantuml.GenerateDiagramsTask
             
            plugins {
                id('io.github.redgreencoding.plantuml')
            }
            
            plantuml {
                options {
                    outputDir = project.file("svg")
                }
            }
                        
            tasks.register('generateDiagrams', GenerateDiagramsTask) {
                source = fileTree(dir: 'puml')
            }
        """

        include(canUseGlobPattern("gradleTasks", "", gradleDSL))

        val kotlinDSL =
            """
            import io.github.redgreencoding.plantuml.GenerateDiagramsTask
            
            plugins {
                id("io.github.redgreencoding.plantuml")
            }
            
            plantuml {
                options {
                    outputDir = project.file("svg")
                }
            }
            
            tasks.register<GenerateDiagramsTask>("generateDiagrams") {
                source.set(fileTree("puml"))    
            }
        """
        include(canUseGlobPattern("kotlinTasks", ".kts", kotlinDSL))
    }

    usingExtension()
    usingTasks()
})

private fun canConfigurePlantUmlPlugin(name: String, extension: String, buildFile: String) =
    stringSpec {
        "can configure plantuml task using $name DSL" {
            // Setup the test build
            val projectDir = File("build/functionalTest/$name")
            projectDir.mkdirs()
            projectDir.resolve("settings.gradle$extension").writeText("")
            projectDir.resolve("build.gradle$extension").writeText(buildFile)
            projectDir.resolve("gradle.properties").writeText("org.gradle.unsafe.configuration-cache=true")

            projectDir.resolve("Hello.puml").writeText(
                """
            @startuml

            Bob->Alice : Hello World!

            @enduml
                """.trimIndent()
            )

            // Run the build
            val runner = GradleRunner.create().apply {
                forwardOutput()
                withPluginClasspath()
                withArguments("--rerun-tasks", "plantumlAll")
                withProjectDir(projectDir)
            }

            val result = runner.build()

            listOf(":plantumlAll", ":plantumlHello", ":plantumlHello2").forEach {
                val task = result.task(it)
                task.shouldNotBeNull()
                task.outcome shouldBe TaskOutcome.SUCCESS
            }

            listOf("Hello.svg", "Hello2.svg").forEach {
                val svg = File(projectDir, "svg/$it")
                svg.shouldExist()

                validateSVG(svg)
            }
        }
    }


private fun canUseGlobPattern(name: String, extension: String, buildFile: String) =
    stringSpec {
        "can use Glob pattern using $name DSL" {
            // Setup the test build
            val projectDir = File("build/functionalTest/$name")
            projectDir.mkdirs()
            projectDir.resolve("settings.gradle$extension").writeText("")
            projectDir.resolve("build.gradle$extension").writeText(buildFile)
            projectDir.resolve("gradle.properties").writeText("org.gradle.unsafe.configuration-cache=true")

            val pumlDir = File(projectDir, "puml")
            pumlDir.mkdirs()
            pumlDir.resolve("Hello.puml").writeText(
                """
            @startuml

            Bob->Alice : Hello World!

            @enduml
                """.trimIndent()
            )

            val pumlSubDir = File(pumlDir, "sub")
            pumlSubDir.mkdirs()

            pumlDir.resolve("Hello2.puml").writeText(
                """
            @startuml

            Bob->Alice : Hello World!

            @enduml
                """.trimIndent()
            )


            // Run the build
            val runner = GradleRunner.create().apply {
                forwardOutput()
                withPluginClasspath()
                withArguments("--rerun-tasks", "generateDiagrams", "--stacktrace")
                withProjectDir(projectDir)
            }

            val result = runner.build()

            listOf(":generateDiagrams").forEach {
                val task = result.task(it)
                task.shouldNotBeNull()
                task.outcome shouldBe TaskOutcome.SUCCESS
            }

            listOf("Hello.svg", "Hello2.svg").forEach {
                val svg = File(projectDir, "svg/$it")
                svg.shouldExist()

                validateSVG(svg)
            }
        }
    }


private fun validateSVG(file: File) {
    file.inputStream().buffered().use {
        val source = StreamSource(it)

        println("Validation of ${file.name} against SVG XSD schema.")
        val start = System.currentTimeMillis()

        SvgValidator(source)
        println("SVG is valid.")

        println("Took (ms): " + (System.currentTimeMillis() - start))
    }
}

private object SvgValidator {
    private val factory: SchemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema")
    private val svgSchema: URL = javaClass.getResource("/SVG.xsd") ?: throw AssertionError("SVG.xsd is missing!")
    private val schema: Schema = factory.newSchema(svgSchema)
    private val validator: Validator = schema.newValidator()

    operator fun invoke(source: StreamSource) = validator.validate(source)
}

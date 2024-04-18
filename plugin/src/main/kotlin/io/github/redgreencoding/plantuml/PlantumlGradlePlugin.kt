package io.github.redgreencoding.plantuml

import groovy.lang.Closure
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.file.FileTree
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.util.*
import javax.inject.Inject

class PlantumlGradlePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val plantuml = project.extensions.create("plantuml", PlantUMLExtension::class.java)

        val generateDiagramsTask = project.tasks.register("plantumlAll") {
            it.group = "plantuml"
            it.description = "Generate all plantuml diagrams"
        }

        plantuml.diagrams.all { diagram ->
            val task = project.tasks.register(
                "plantuml${diagram.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}",
                GenerateDiagramTask::class.java,
                diagram,
                plantuml.options
            )

            task.configure {
                it.description = "Generate plantuml diagram ${diagram.name}"
                it.group = "plantuml"
            }

            generateDiagramsTask.configure {
                it.dependsOn(task)
            }
        }
    }
}

class Options(project: Project) {

    /** Where to generate the output image. */
    @Internal
    var outputDir: File = project.file("${project.layout.buildDirectory}/plantuml")

    /** Output format. */
    @Input
    var format: String = "svg"

    /** Gets PlantUML file format from given format. */
    fun fileFormat(): FileFormat =
        FileFormat.valueOf(format.uppercase(Locale.getDefault()))
}

/**
 * A diagram to generate.
 */
class Diagram(
    project: Project,

    /** The diagram unique name. */
    @Input
    val name: String,
) {
    /** Source file. */
    @InputFile
    @PathSensitive(PathSensitivity.RELATIVE)
    var sourceFile: File = project.file("$name.puml")
}

internal typealias DiagramsContainer = NamedDomainObjectContainer<Diagram>

open class PlantUMLExtension(project: Project) {

    internal val options = Options(project)

    internal val diagrams = project.container(Diagram::class.java) { name ->
        Diagram(project, name)
    }

    fun diagrams(config: DiagramsContainer.() -> Unit) {
        diagrams.configure(object : Closure<Unit>(this, this) {
            fun doCall() {
                @Suppress("UNCHECKED_CAST")
                (delegate as? DiagramsContainer)?.let {
                    config(it)
                }
            }
        })
    }

    fun diagrams(config: Closure<Unit>) {
        diagrams.configure(config)
    }

    fun options(config: Closure<Unit>) {
        config.delegate = options
        config.call()
    }

    fun options(config: Options.() -> Unit) {
        options.config()
    }
}

/**
 * The task generating an image from a plantuml source file.
 */
@CacheableTask
open class GenerateDiagramTask @Inject constructor(
    @Nested
    val diagram: Diagram,

    @Nested
    val options: Options
) : DefaultTask() {

    @OutputFile
    fun getOutputFile() =
        File(options.outputDir, "${diagram.name}${options.fileFormat().fileSuffix}")

    @TaskAction
    fun generate() {
        val reader = SourceStringReader(diagram.sourceFile.readText())

        val parentFile = getOutputFile().parentFile
        if (!parentFile.exists() && !parentFile.mkdirs())
            throw GradleException("Unable to create ${getOutputFile().parentFile} directory")

        getOutputFile().outputStream().buffered().use {
            reader.outputImage(it, FileFormatOption(options.fileFormat()))
        }
    }
}

/**
 * The task generating an image from a plantuml source file.
 */
@CacheableTask
abstract class GenerateDiagramsTask : DefaultTask() {

    init {
        group = "plantuml"
        description = "Generate plantuml diagrams"
    }

    private val options: Options =
        project.extensions.getByType(PlantUMLExtension::class.java).options

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val source: Property<FileTree>

    @get:OutputDirectory
    internal val outputDirectory =
        options.outputDir

    @TaskAction
    fun generate() {
        if (!outputDirectory.exists() && !outputDirectory.mkdirs())
            throw GradleException("Unable to create $outputDirectory directory")

        source.get().visit { fileDetails ->

            val file = fileDetails.file

            if (!file.isFile) return@visit

            val fileName = fileDetails.name
            val simpleName = fileName.substring(0, fileName.lastIndexOf("."))
            val outputFile = File(outputDirectory, "${simpleName}${options.fileFormat().fileSuffix}")

            fileDetails.open().bufferedReader().use { reader ->
                val stringReader = SourceStringReader(reader.readText())

                outputFile.outputStream().buffered().use {
                    stringReader.outputImage(it, FileFormatOption(options.fileFormat()))
                }
            }
        }
    }
}

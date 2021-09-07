# plantuml-gradle-plugin

This plugin allows you to convert [PlantUML](https://plantuml.com) (_.puml_) files into one of the supported output formats:
* _.svg_
* _.png_
* ... (check [here](https://github.com/plantuml/plantuml/blob/master/src/net/sourceforge/plantuml/FileFormat.java#L64) for all supported formats of the underlying  [PlantUML library](https://github.com/plantuml/plantuml) library)

## Adding the plugin

### Kotlin

Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):
```kotlin
plugins {
    id("com.github.redgreencoding.plantuml") version "x.y.z"
}
```

Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):
````kotlin
buildscript {
  repositories {
    maven {
      url = uri("https://plugins.gradle.org/m2/")
    }
  }
  dependencies {
    classpath("com.github.redgreencoding:plantuml-gradle-plugin:x.y.z")
  }
}

apply(plugin = "com.github.redgreencoding.plantuml")
````
### Groovy

Using the [plugins DSL](https://docs.gradle.org/current/userguide/plugins.html#sec:plugins_block):
```groovy
plugins {
    id "com.github.redgreencoding.plantuml" version "x.y.z"
}
```

Using [legacy plugin application](https://docs.gradle.org/current/userguide/plugins.html#sec:old_plugin_application):
````groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.redgreencoding:plantuml-gradle-plugin:x.y.z"
  }
}

apply plugin: "com.github.redgreencoding.plantuml"
````

## Plugin usage

After [adding the plugin](#adding-the-plugin) to your build you can configure the plugin using the extension block `plantuml` 

Check the supported output formats [here](https://github.com/plantuml/plantuml/blob/master/src/net/sourceforge/plantuml/FileFormat.java#L64).

### Kotlin

```kotlin
plantuml {
    options {
        // where should the .svg be generated to (defaults to build/plantuml)
        outputDir = project.file("svg")
        
        // output format (lowercase, defaults to svg)
        format = "svg"
    }

    diagrams {
        create("File1") {
            // .puml sourcefile, this can be also omitted and defaults to _<name>.puml_.
            sourceFile = project.file("doc/File1.puml")
        }

        // this will just look for the file File2.puml
        create("File2")

        // add additional files here
    }
}
```

### Groovy

```groovy
plantuml {
    options {
        // where should the .svg be generated to (defaults to build/plantuml)
        outputDir = project.file("svg")

        // output format (lowercase, defaults to svg)
        format = "svg"
    }

    diagrams {
        File1 {
            // .puml sourcefile, this can be also omitted and defaults to _<name>.puml_.
            sourceFile = project.file("doc/File1.puml")
        }

        // this will just look for the file File2.puml
        File2

        // add additional files here
    }
}
```
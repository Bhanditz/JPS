package org.jetbrains.jps.kotlin

import org.jetbrains.jps.ModuleBuilder
import org.jetbrains.jps.ModuleBuildState
import org.jetbrains.jps.ModuleChunk
import org.jetbrains.jps.Project
import groovy.io.FileType

/**
 * @author max
 */
class KotlinModuleBuilder implements ModuleBuilder {
    def processModule(ModuleBuildState state, ModuleChunk moduleChunk, Project project) {
        // TODO: Attempt to compile standard library results in "duplicate declaration" since we do have those in compiler embedded
        if ("stdlib" == moduleChunk.getName()) return
        if ("examples" == moduleChunk.getName()) return

        List<File> kotlinFiles = []
        state.sourceRoots.each {
            scanKotlinFiles(new File(it), kotlinFiles)
        }

        def ant = project.binding.ant

        if (!kotlinFiles.empty) {
            String kotlinHome = project.getPropertyIfDefined("kotlinHome")

            if (kotlinHome == null) {
                ant.fail("kotlinHome is not defined")
            }

            if (!new File(kotlinHome, "lib/kotlin-compiler.jar").exists()) {
                ant.fail("'$kotlinHome' is not a valid Kotlin compiler. Can't find lib/kotlin-compiler.jar there")
            }

            ant.mkdir(dir: state.targetFolder)

            StringBuilder builder = new StringBuilder()
            builder.append("import kotlin.modules.*\n")
            builder.append("val modules = module(\"${moduleChunk.name}\") {\n")

            kotlinFiles.each {
                builder.append("source files \"${it.absolutePath}\"\n")
            }

            state.classpath.each {
                if (new File(it).exists()) {
                    builder.append("classpath entry \"${it}\"\n")
                }
            }

            builder.append("}\n")

            def moduleFile = new File(state.targetFolder, "module.kts")
            moduleFile.text = builder.toString()

            def jarName = "${state.targetFolder}/kt.jar"
            ant.java(classname: "org.jetbrains.jet.cli.KotlinCompiler", fork: "true") {
                jvmarg(line: "-ea -Xmx300m -XX:MaxPermSize=200m")

                arg(value: "-module")
                arg(value: "${moduleFile.absolutePath}")

                arg(value: "-jar")
                arg(value: jarName)

                classpath() {
                    fileset(dir: "$kotlinHome/lib") {
                        include(name: "*.jar")
                    }
                }
            }

            moduleFile.delete();

            state.classpath << jarName
            ant.unjar(src: jarName, dest: state.targetFolder)
        }
    }

    def scanKotlinFiles(File file, List<File> answer) {
        file.eachFileRecurse(FileType.FILES) {
            if (it.getName().endsWith(".kt")) {
                answer.add(it)
            }
        }
    }
}

package com.vimeo.stag.processor

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import java.io.File
import java.util.Locale
import javax.annotation.processing.Processor
import javax.tools.DiagnosticCollector
import javax.tools.ToolProvider
import kotlin.reflect.KClass

/**
 * A class that compiles [KClass] using the provided [processor] and emits the compilation result.
 *
 * Created by restainoa on 9/27/17.
 */
class ProcessorTester(private val processor: () -> Processor, private vararg val options: String) {

    private val fileManager = ToolProvider.getSystemJavaCompiler().getStandardFileManager(DiagnosticCollector(), null, null)
    private val classpathRoot = File(Thread.currentThread().contextClassLoader.getResource("").path)
    private val projectRoot = classpathRoot.parentFile.parentFile.parentFile
    private val javaRoot = File(File(File(projectRoot, "src"), "test"), "java")

    /**
     * Compile the provided class(es) and return the [Compilation] result.
     *
     * @param clazz the class(es) to compile.
     */
    fun compile(vararg clazz: KClass<*>): Compilation {
        return Compiler.javac()
                .withProcessors(processor())
                .withOptions(*options)
                .compile(fileManager.getJavaFileObjects(*clazz.map { it.asResourcePath().asClassPathFile() }.toTypedArray()))
                .printError()
    }

    /**
     * Compile the provided class(es) and return the [Compilation] result.
     *
     * @param clazz the class(es) to compile, in the form of `package/ClassName.java`
     */
    fun compileResource(vararg clazz: String): Compilation {
        return Compiler.javac()
                .withProcessors(processor())
                .withOptions(*options)
                .compile(fileManager.getJavaFileObjects(*clazz.map { javaClass.getResource(it).toURI() }.map { File(it) }.toTypedArray()))
                .printError()
    }

    fun compileClassInModule(module: String, vararg clazz: KClass<*>): Compilation {
        val modulePath = classpathRoot.parentFile.parentFile.parentFile.parentFile.parentFile.path + "/" + module + "/"
        val srcRoot = File(File(File(modulePath, "src"), "main"), "java")
        return Compiler.javac()
                .withProcessors(processor())
                .withOptions(*options)
                .compile(fileManager.getJavaFileObjectsFromFiles(
                        clazz.map { it.java }
                                .map { it.name.replace(".", "/") }
                                .map { "$it.java" }
                                .map { File(srcRoot.path + "/" + it) })
                )
                .printError()
    }

    /**
     * Print any errors that are emitted by the compilation so that we can see them during tests.
     */
    private fun Compilation.printError(): Compilation {
        errors().forEach { println(it.getMessage(Locale.ROOT)) }
        return this
    }

    /**
     * Converts the [KClass] to a java resource path.
     */
    private fun KClass<*>.asResourcePath() = "${this.java.name.replace('.', '/')}.java"

    /**
     * Converts a [String] representing a resource path to a [File].
     */
    private fun String.asClassPathFile(): File {
        val ind = this.indexOf('$')

        return if (ind < 0) {
            File(javaRoot, this)
        } else {
            File(javaRoot, "${this.substring(0, ind)}.java")
        }
    }
}

/**
 * Return `true` if the status of the compilation is [Compilation.Status.SUCCESS], `false`
 * otherwise.
 */
fun Compilation.isSuccessful() = this.status() == Compilation.Status.SUCCESS
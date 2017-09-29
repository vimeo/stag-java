package com.vimeo.stag.processor

import com.google.testing.compile.Compilation
import com.google.testing.compile.Compiler
import java.io.File
import javax.annotation.processing.Processor
import javax.tools.DiagnosticCollector
import javax.tools.ToolProvider
import kotlin.reflect.KClass

/**
 * A class that compiles [KClass] using the provided [processor] and emits the compilation result.
 *
 * Created by restainoa on 9/27/17.
 */
class ProcessorTester(private val processor: () -> Processor) {

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
                .compile(fileManager.getJavaFileObjects(*clazz.map { it.asResourcePath().asClassPathFile() }.toTypedArray()))
    }

    /**
     * Compile the provided class(es) and return the [Compilation] result.
     *
     * @param clazz the class(es) to compile, in the form of `package/ClassName.java`
     */
    fun compile(vararg clazz: String): Compilation {
        return Compiler.javac()
                .withProcessors(processor())
                .compile(fileManager.getJavaFileObjects(*clazz.map { it.asClassPathFile() }.toTypedArray()))
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
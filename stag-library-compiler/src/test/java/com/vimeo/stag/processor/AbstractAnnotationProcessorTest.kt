/*
 * Adapted from: AbstractAnnotationProcessorTest.java     5 Jun 2009
 *
 * Copyright © 2010 Andrew Phillips.
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ====================================================================
 */
package com.vimeo.stag.processor

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import java.io.File
import java.io.IOException
import java.lang.IllegalArgumentException
import java.net.URISyntaxException
import java.util.*
import javax.annotation.processing.Processor
import javax.tools.*
import javax.tools.Diagnostic.Kind
import kotlin.collections.ArrayList

/**
 * Adapted from [https://raw.githubusercontent.com/ngs-doo/dsl-json/master/processor/src/test/java/com/dslplatform/json/AbstractAnnotationProcessorTest.java]
 *
 * Taken from https://github.com/anthonycr/Mezzanine/blob/66349b65e73ce58235353542aaeaefb3adc30b59/mezzanine-compiler/src/test/java/com/anthonycr/mezzanine/AbstractAnnotationProcessorTest.kt
 *
 * A base test class for [annotation processor][Processor] testing that
 * attempts to compile source test cases that can be found on the classpath.
 *
 * @author aphillips
 * @since 5 Jun 2009
 */
abstract class AbstractAnnotationProcessorTest {

    /**
     * @return the processor instances that should be tested
     */
    protected abstract fun getProcessors(): Collection<Processor>

    /**
     * Attempts to compile the given compilation units using the Java Compiler
     * API.
     *
     * The compilation units and all their dependencies are expected to be on
     * the classpath.
     *
     * @param compilationUnits the classes to compile
     * @return the [diagnostics][Diagnostic] returned by the compilation,
     *  as demonstrated in the documentation for [JavaCompiler]
     * @see .compileTestCase
     */
    protected fun compileTestCase(vararg compilationUnits: Class<*>) =
            compileTestCase(Arrays.asList("-Adsljson.showdsl=true"), *compilationUnits)

    protected fun compileTestCase(vararg compilationUnitResources: String): List<Diagnostic<out JavaFileObject>> =
            compileTestCase(classNamesToFiles(*compilationUnitResources), listOf())

    /**
     * Attempts to compile the given compilation units using the Java Compiler
     * API.
     *
     * The compilation units and all their dependencies are expected to be on
     * the classpath.
     *
     * @param compileArguments compile arguments to pass into annotation processing
     * @param compilationUnits the classes to compile
     * @return the [diagnostics][Diagnostic] returned by the compilation,
     * as demonstrated in the documentation for [JavaCompiler]
     * @see .compileTestCase
     */
    protected fun compileTestCase(compileArguments: List<String>,
                                  vararg compilationUnits: Class<*>): List<Diagnostic<out JavaFileObject>> {
        val compilationUnitPaths = compilationUnits.map { toResourcePath(it) }.toTypedArray()

        return compileTestCase(compileArguments, *compilationUnitPaths)
    }

    protected fun compileTestCase(compileArguments: List<String>,
                                  vararg compilationUnitPaths: String): List<Diagnostic<out JavaFileObject>> {
        return try {
            compileTestCase(findClasspathFiles(compilationUnitPaths), compileArguments)
        } catch (exception: IOException) {
            throw IllegalArgumentException(
                    "Unable to resolve compilation units ${Arrays.toString(compilationUnitPaths)} due to: ${exception.message}",
                    exception
            )
        }
    }

    /**
     * Attempts to compile the given compilation units using the Java Compiler API.
     *
     * The compilation units and all their dependencies are expected to be on the classpath.
     *
     * @param compilationUnits the paths of the source files to compile, as would be expected
     * by [ClassLoader.getResource]
     * @return the [diagnostics][Diagnostic] returned by the compilation, as demonstrated in the
     * documentation for [JavaCompiler]
     * @see .compileTestCase
     */
    private fun compileTestCase(compilationUnits: Collection<File>,
                                arguments: List<String>): List<Diagnostic<out JavaFileObject>> {
        val diagnosticCollector = DiagnosticCollector<JavaFileObject>()

        val javaFileManager = COMPILER.getStandardFileManager(diagnosticCollector, null, null)
        val cleanableFileManager = CleanableJavaFileManager(javaFileManager)

        val compileArgs = listOf("-proc:only") + arguments
        /*
         * Call the compiler with the "-proc:only" option. The "class names"
         * option (which could, in principle, be used instead of compilation
         * units for annotation processing) isn't useful in this case because
         * only annotations on the classes being compiled are accessible.
         *
         * Information about the classes being compiled (such as what they are annotated
         * with) is *not* available via the RoundEnvironment. However, if these classes
         * are annotations, they certainly need to be validated.
         */
        val task = COMPILER.getTask(null, cleanableFileManager, diagnosticCollector,
                compileArgs, null,
                javaFileManager.getJavaFileObjectsFromFiles(compilationUnits))
        task.setProcessors(getProcessors())
        task.call()

        try {
            cleanableFileManager.close()
        } catch (ignore: IOException) {
        }

        val diagnostics = diagnosticCollector.diagnostics
        if (diagnostics.isNotEmpty() && diagnostics[0].kind == Kind.WARNING
                && diagnostics[0].getMessage(Locale.ENGLISH).startsWith("Supported source version 'RELEASE_6' from annotation processor 'com.dslplatform.json.CompiledJsonProcessor' less than -source")) {
            return diagnostics.subList(1, diagnostics.size)
        }

        return diagnostics
    }

    companion object {
        private const val SOURCE_FILE_SUFFIX = ".java"
        private val COMPILER = ToolProvider.getSystemJavaCompiler()

        @JvmStatic
        private fun toResourcePath(clazz: Class<*>) = clazz.name.replace('.', '/') + SOURCE_FILE_SUFFIX

        @JvmStatic
        private fun classNamesToFiles(vararg classNames: String): Collection<File> = classNames.map { resourceToFile(it + SOURCE_FILE_SUFFIX) }

        @JvmStatic
        private fun resourceToFile(resourceName: String): File {
            val resource = this::class.java.getResource(resourceName)
            assert(resource.protocol == "file")
            return try {
                File(resource.toURI())
            } catch (e: URISyntaxException) {
                throw e
            }

        }

        @JvmStatic
        @Throws(IOException::class)
        private fun findClasspathFiles(filenames: Array<out String>): Collection<File> {
            val classpathFiles = ArrayList<File>(filenames.size)

            val cl = Thread.currentThread().contextClassLoader
            val classpathRoot = File(cl.getResource("").path)
            val projectRoot = classpathRoot.parentFile.parentFile.parentFile
            val javaRoot = File(File(File(projectRoot, "src"), "test"), "java")

            for (filename in filenames) {
                val ind = filename.indexOf('$')
                if (ind < 0) {
                    classpathFiles.add(File(javaRoot, filename))
                } else {
                    classpathFiles.add(File(javaRoot, filename.substring(0, ind) + ".java"))
                }
            }

            return classpathFiles
        }

        /**
         * Asserts that the compilation produced no errors, i.e. no diagnostics of type
         * [Kind.ERROR].
         *
         * @param diagnostics the result of the compilation
         * @see .assertCompilationReturned
         * @see .assertCompilationReturned
         */
        @JvmStatic
        protected fun assertCompilationSuccessful(diagnostics: List<Diagnostic<out JavaFileObject>>) =
                diagnostics.forEach { assertFalse(it.getMessage(Locale.ENGLISH), it.kind == Kind.ERROR) }

        /**
         * Asserts that the compilation produced results of the following
         * [Kinds][Kind] at the given line numbers, where the *n*th kind
         * is expected at the *n*th line number.
         *
         * Does not check that these is the *only* diagnostic kinds returned!
         *
         * @param expectedDiagnosticKinds the kinds of diagnostic expected
         * @param expectedLineNumbers     the line numbers at which the diagnostics are expected
         * @param diagnostics             the result of the compilation
         * @see .assertCompilationSuccessful
         * @see .assertCompilationReturned
         */
        @JvmStatic
        protected fun assertCompilationReturned(expectedDiagnosticKinds: Array<Kind>,
                                                expectedLineNumbers: LongArray,
                                                diagnostics: List<Diagnostic<out JavaFileObject>>) {
            assert(expectedDiagnosticKinds.size == expectedLineNumbers.size)

            for (i in expectedDiagnosticKinds.indices) {
                assertCompilationReturned(expectedDiagnosticKinds[i], expectedLineNumbers[i], diagnostics, "")
            }
        }

        /**
         * Asserts that the compilation produced a result of the following
         * [Kind] at the given line number.
         *
         * Does not check that this is the *only* diagnostic kind returned!
         *
         * @param expectedDiagnosticKind the kind of diagnostic expected
         * @param expectedLineNumber     the line number at which the diagnostic is expected
         * @param diagnostics            the result of the compilation
         * @param contains               diagnostics results contains the message
         * @see .assertCompilationSuccessful
         * @see .assertCompilationReturned
         */
        @JvmStatic
        protected fun assertCompilationReturned(expectedDiagnosticKind: Kind, expectedLineNumber: Long,
                                                diagnostics: List<Diagnostic<out JavaFileObject>>,
                                                contains: String): Diagnostic<*> {

            val detected = diagnostics.firstOrNull { it.kind == expectedDiagnosticKind && it.lineNumber == expectedLineNumber }

            val nonNullDetected = requireNotNull(detected) {
                "Expected a result of kind $expectedDiagnosticKind at line $expectedLineNumber"
            }

            assertTrue(nonNullDetected.getMessage(Locale.ENGLISH).contains(contains))

            return nonNullDetected
        }
    }

}
package com.vimeo.stag.processor;

/*
 * @(#)AbstractAnnotationProcessorTest.java     5 Jun 2009
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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import static com.google.common.base.Throwables.propagate;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * A base test class for {@link Processor annotation processor} testing that
 * attempts to compile source test cases that can be found on the classpath.
 *
 * Original source from:
 *   https://github.com/ngs-doo/dsl-json/blob/master/processor/src/test/java/com/dslplatform/json/AbstractAnnotationProcessorTest.java
 *
 * @author aphillips
 * @since 5 Jun 2009
 */
abstract class AbstractAnnotationProcessorTest {
    private static final String SOURCE_FILE_SUFFIX = ".java";
    private static final JavaCompiler COMPILER = ToolProvider.getSystemJavaCompiler();

    /** @return the processor instances that should be tested */
    protected abstract Collection<Processor> getProcessors();

    protected List<Diagnostic<? extends JavaFileObject>> compileTestCase(String ... classNames) {
        DiagnosticCollector<JavaFileObject> diagnosticCollector = new DiagnosticCollector<>();
        try (StandardJavaFileManager fileManager = COMPILER.getStandardFileManager(diagnosticCollector, null, null);
            CleanableJavaFileManager cleanableJavaFileManager = new CleanableJavaFileManager(fileManager)) {
            Collection<File> files = classNamesToFiles(classNames);
            Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjectsFromFiles(files);
            /*
             * Call the compiler with the "-proc:only" option. The "class names"
             * option (which could, in principle, be used instead of compilation
             * units for annotation processing) isn't useful in this case because
             * that only gives access to the annotation declaration, not the
             * members.
             */
            CompilationTask task = COMPILER.getTask(null, cleanableJavaFileManager, diagnosticCollector,
                    Collections.singletonList("-proc:only"), null, javaFileObjects);
            task.setProcessors(getProcessors());
            task.call();

        } catch (IOException exception) {
            exception.printStackTrace(System.err);
        }

        return diagnosticCollector.getDiagnostics();
    }

    private Collection<File> classNamesToFiles(String ... classNames) {
        ArrayList<File> files = new ArrayList<>(classNames.length);
        for (String className : classNames) {
            files.add(resourceToFile(className + SOURCE_FILE_SUFFIX));
        }
        return files;
    }

    private File resourceToFile(String resourceName) {
        URL resource = getClass().getResource(resourceName);
        assert resource.getProtocol().equals("file");
        try {
            return new File(resource.toURI());
        } catch (URISyntaxException e) {
            throw propagate(e);
        }
    }

    protected static void assertCompilationSuccessful(List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        assert (diagnostics != null);

        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            System.err.println("Diagnostic: " + diagnostic);
            assertFalse("Expected no errors", diagnostic.getKind().equals(Kind.ERROR));
        }
    }

    protected static void assertCompilationReturned(Kind expectedDiagnosticKind, long expectedLineNumber,
        List<Diagnostic<? extends JavaFileObject>> diagnostics) {
        assert ((expectedDiagnosticKind != null) && (diagnostics != null));
        boolean expectedDiagnosticFound = false;

        StringBuilder diagnosticsPrintout = new StringBuilder();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
            diagnosticsPrintout.append(diagnostic.getKind())
                    .append(" - ")
                    .append(getSourceIdent(diagnostic))
                    .append(" - ")
                    .append(diagnostic.getMessage(Locale.ENGLISH))
                    .append("\n");
            if (diagnostic.getKind().equals(expectedDiagnosticKind)
                && (diagnostic.getLineNumber() == expectedLineNumber)) {
                expectedDiagnosticFound = true;
                break;
            }
        }
        if (diagnosticsPrintout.length() == 0) {
            diagnosticsPrintout.append("No diagnostic result");
        }
        assertTrue("Expected a result of kind " + expectedDiagnosticKind
            + " at line " + expectedLineNumber + "\nFound instead:\n" + diagnosticsPrintout,
            expectedDiagnosticFound);
    }

    private static String getSourceIdent(Diagnostic<? extends JavaFileObject> diagnostic) {
        JavaFileObject source = diagnostic.getSource();
        if (source == null) {
            return "unknown";
        }
        String name = source.getName();
        int indexOf = name.lastIndexOf(File.separatorChar);
        if (indexOf >= 0) {
            name = name.substring(indexOf + 1);
        }
        long lineNumber = diagnostic.getLineNumber();
        long columnNumber = diagnostic.getColumnNumber();
        return name + ":" + lineNumber + ":" + columnNumber;
    }
}

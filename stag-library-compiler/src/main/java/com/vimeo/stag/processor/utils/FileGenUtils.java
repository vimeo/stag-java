/*
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Vimeo
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.vimeo.stag.processor.utils;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

public final class FileGenUtils {

    public static final String UNESCAPED_SEPARATOR = "$";
    private static final String CODE_BLOCK_ESCAPED_SEPARATOR = "$$";

    private FileGenUtils() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    /**
     * Writes a Java file to the file system after
     * deleting the previous copy.
     *
     * @param file  the file to write.
     * @param filer the Filer to use to do the writing.
     * @throws IOException throws an exception if we are unable
     *                     to write the file to the filesystem.
     */
    public static void writeToFile(@NotNull JavaFile file, @NotNull Filer filer) throws IOException {
        String fileName =
                file.packageName.isEmpty() ? file.typeSpec.name : file.packageName + '.' + file.typeSpec.name;
        List<Element> originatingElements = file.typeSpec.originatingElements;
        JavaFileObject filerSourceFile = filer.createSourceFile(fileName, originatingElements.toArray(
                new Element[originatingElements.size()]));
        filerSourceFile.delete();
        Writer writer = null;
        try {
            writer = filerSourceFile.openWriter();
            file.writeTo(writer);
        } catch (Exception e) {
            try {
                filerSourceFile.delete();
            } catch (Exception ignored) {
            }
            throw e;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    static CharSequence readResource(@NotNull Filer filer, @NotNull String generatedPackageName,
                                     @NotNull String resourceName) throws IOException {
        try {
            FileObject file =
                    filer.getResource(StandardLocation.CLASS_OUTPUT, generatedPackageName, resourceName);
            return file.getCharContent(false);
        } catch (FileNotFoundException e) {
            DebugLog.log("Resource not found: " + resourceName);
            return null;
        }
    }

    static void writeToResource(@NotNull Filer filer, @NotNull String generatedPackageName,
                                @NotNull String resourceName, @NotNull CharSequence content)
            throws IOException {
        FileObject file =
                filer.createResource(StandardLocation.CLASS_OUTPUT, generatedPackageName, resourceName);
        file.delete();
        Writer writer = null;
        try {
            writer = file.openWriter();
            writer.append(content);
            DebugLog.log("Wrote to resource '" + resourceName + "':\n" + content);
        } catch (Exception e) {
            try {
                file.delete();
            } catch (Exception ignored) {
            }
            throw e;
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignored) {

                }
            }
        }
    }

    /**
     * Safely closes a closeable.
     *
     * @param closeable object to close.
     */
    static void close(@Nullable Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException e) {
            // ignored
        }
    }

    /**
     * Takes a String input and escapes it for use
     * in the {@link CodeBlock} class.
     *
     * @param string the string to escape.
     * @return a String safe to use in a {@link CodeBlock}
     */
    @NotNull
    public static String escapeStringForCodeBlock(@NotNull String string) {
        return string.replace(UNESCAPED_SEPARATOR, CODE_BLOCK_ESCAPED_SEPARATOR);
    }

    /**
     * Takes a String input that was escaped for
     * use in the {@link CodeBlock} class and
     * unescapes it for normal use.
     *
     * @param string the String to unescape,
     * @return a String safe for normal use.
     */
    @NotNull
    public static String unescapeEscapedString(@NotNull String string) {
        return string.replace(CODE_BLOCK_ESCAPED_SEPARATOR, UNESCAPED_SEPARATOR);
    }

}

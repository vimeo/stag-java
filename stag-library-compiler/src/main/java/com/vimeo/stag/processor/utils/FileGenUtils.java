package com.vimeo.stag.processor.utils;

import com.squareup.javapoet.JavaFile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

/**
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
public final class FileGenUtils {

    public static final String GENERATED_PACKAGE_NAME = "com.vimeo.stag.generated";

    private FileGenUtils() {
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

}

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

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public final class KnownTypeAdapterFactoriesUtils {

    private static final String KNOWN_FACTORIES_RESOURCE = "StagTypeAdapterFactory.list";

    private KnownTypeAdapterFactoriesUtils() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    public static Set<TypeMirror> loadKnownTypes(@NotNull ProcessingEnvironment processingEnv, @NotNull String generatedPackageName)
            throws IOException {
        Filer filer = processingEnv.getFiler();
        LinkedHashSet<TypeMirror> knownTypes = new LinkedHashSet<>();
        Elements elementUtils = processingEnv.getElementUtils();
        loadKnownTypesFromFiler(elementUtils, filer, generatedPackageName, knownTypes);
        loadKnownTypesFromClasspath(elementUtils, knownTypes, generatedPackageName);

        // Filter out types which used to be present but are no longer available:
        Iterator<TypeMirror> iterator = knownTypes.iterator();
        while (iterator.hasNext()) {
            TypeMirror knownType = iterator.next();
            if (knownType == null) {
                iterator.remove();
            }
        }

        return knownTypes;
    }

    public static void writeKnownTypes(@NotNull ProcessingEnvironment processingEnv, @NotNull String generatedPackageName,
                                       @NotNull Set<TypeMirror> knownTypes) throws IOException {
        Filer filer = processingEnv.getFiler();
        StringBuilder knownTypesBuilder = new StringBuilder();
        for (TypeMirror knownType : knownTypes) {
            knownTypesBuilder.append(knownType).append("\n");
        }
        FileGenUtils.writeToResource(filer, generatedPackageName, KNOWN_FACTORIES_RESOURCE, knownTypesBuilder.toString());
    }

    private static void loadKnownTypesFromFiler(@NotNull Elements elementUtils, @NotNull Filer filer, @NotNull String generatedPackageName, @NotNull Set<TypeMirror> resultSet)
            throws IOException {
        CharSequence content = FileGenUtils.readResource(filer, generatedPackageName, KNOWN_FACTORIES_RESOURCE);
        if (content == null) {
            return;
        }
        String[] knownFactories = content.toString().split("[\\n\\r]+");
        for(String knownFactory : knownFactories) {
            TypeElement element = elementUtils.getTypeElement(knownFactory);
            if(null != element) {
                resultSet.add(element.asType());
            }
        }
    }

    private static void loadKnownTypesFromClasspath(@NotNull Elements elementUtils, @NotNull Set<TypeMirror> resultSet, @NotNull String generatedPackageName) throws IOException {
        ClassLoader classLoader = KnownTypeAdapterFactoriesUtils.class.getClassLoader();
        String resourcePath = generatedPackageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(resourcePath + "/" + KNOWN_FACTORIES_RESOURCE);
        while (resources.hasMoreElements()) {
            URL typeAdapterFactoryUrl = resources.nextElement();
            InputStream inputStream = typeAdapterFactoryUrl.openStream();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    TypeElement element = elementUtils.getTypeElement(line.trim());
                    if(null != element) {
                        resultSet.add(element.asType());
                    }

                }
            } finally {
                FileGenUtils.close(inputStream);
            }
        }
    }

}
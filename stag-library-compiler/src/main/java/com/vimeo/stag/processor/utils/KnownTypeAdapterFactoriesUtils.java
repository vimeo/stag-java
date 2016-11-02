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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class KnownTypeAdapterFactoriesUtils {

    private static final String KNOWN_FACTORIES_RESOURCE = "StagTypeAdapterFactory.list";

    public static Set<String> loadKnownTypes(ProcessingEnvironment processingEnv) throws IOException {
        Filer filer = processingEnv.getFiler();
        LinkedHashSet<String> knownTypes = new LinkedHashSet<>();
        loadKnownTypesFromFiler(filer, knownTypes);
        loadKnownTypesFromClasspath(knownTypes);

        // Filter out types which used to be present but are no longer available:
        Elements elementUtils = processingEnv.getElementUtils();
        Iterator<String> iterator = knownTypes.iterator();
        while (iterator.hasNext()) {
            String knownType = iterator.next();
            TypeElement typeElement = elementUtils.getTypeElement(knownType);
            if (typeElement == null) {
                iterator.remove();
            }
        }

        return knownTypes;
    }

    public static void writeKnownTypes(ProcessingEnvironment processingEnv, Set<String> knownTypes)
            throws IOException {
        Filer filer = processingEnv.getFiler();
        StringBuilder knownTypesBuilder = new StringBuilder();
        for (String knownType : knownTypes) {
            knownTypesBuilder.append(knownType).append("\n");
        }
        FileGenUtils.writeToResource(filer, KNOWN_FACTORIES_RESOURCE, knownTypesBuilder.toString());
    }

    private static void loadKnownTypesFromFiler(Filer filer, Set<String> resultSet) throws IOException {
        CharSequence content = FileGenUtils.readResource(filer, KNOWN_FACTORIES_RESOURCE);
        if (content == null) {
            return;
        }
        String[] knownFactories = content.toString().split("[\\n\\r]+");
        for (String knownFactory : knownFactories) {
            resultSet.add(knownFactory);
        }
    }

    private static void loadKnownTypesFromClasspath(Set<String> resultSet) throws IOException {
        ClassLoader classLoader = KnownTypeAdapterFactoriesUtils.class.getClassLoader();
        String resourcePath = FileGenUtils.GENERATED_PACKAGE_NAME.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(resourcePath + "/" + KNOWN_FACTORIES_RESOURCE);
        while (resources.hasMoreElements()) {
            URL typeAdapterFactoryUrl = resources.nextElement();
            InputStream inputStream = typeAdapterFactoryUrl.openStream();
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    resultSet.add(line.trim());
                }
            } finally {
                inputStream.close();
            }
        }
    }

}

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
package com.vimeo.stag.processor.generators;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.utils.FileGenUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class StagGenerator {

    public static final String CLASS_STAG = "Stag";
    private static final String CLASS_TYPE_ADAPTER_FACTORY = "Factory";

    @NotNull
    private final Filer mFiler;

    @NotNull
    private Set<String> mKnownTypeAdapterFactories = new HashSet<>();

    public StagGenerator(@NotNull Filer filer, @NotNull Set<String> knownTypes) {
        mFiler = filer;

        for (String knownType : knownTypes) {
            mKnownTypeAdapterFactories.add(knownType + TypeAdapterFactoryGenerator.CLASS_SUFFIX_FACTORY);
        }
    }

    /**
     * Generates the type adapters and the
     * type adapter factory to be used by
     * the consumer of this library.
     *
     * @throws IOException throws an exception
     *                     if we are unable to write the file
     *                     to the filesystem.
     */
    public void generateTypeAdapters() throws IOException {
        TypeSpec.Builder adaptersBuilder =
                TypeSpec.classBuilder(CLASS_STAG).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        adaptersBuilder.addType(getAdapterFactorySpec());

        JavaFile javaFile =
                JavaFile.builder(FileGenUtils.GENERATED_PACKAGE_NAME, adaptersBuilder.build()).build();

        FileGenUtils.writeToFile(javaFile, mFiler);
    }

    @NotNull
    private TypeSpec getAdapterFactorySpec() {
        TypeVariableName genericTypeName = TypeVariableName.get("T");
        TypeName factoryTypeName = TypeVariableName.get(TypeAdapterFactory.class);

        ParameterizedTypeName factoryListTypeName =
                ParameterizedTypeName.get(ClassName.get(List.class), factoryTypeName);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addCode("mTypeAdapterFactories = java.util.Arrays.<TypeAdapterFactory>asList(\n");
        Iterator<String> knownFactoriesIterator = mKnownTypeAdapterFactories.iterator();
        while (knownFactoriesIterator.hasNext()) {
            String knownFactory = knownFactoriesIterator.next();
            constructorBuilder.addCode("\tnew " + knownFactory + "()");
            if (knownFactoriesIterator.hasNext()) {
                constructorBuilder.addCode(",\n");
            } else {
                constructorBuilder.addCode(");\n");
            }
        }
        MethodSpec constructorSpec = constructorBuilder.build();

        TypeSpec.Builder adapterFactoryBuilder = TypeSpec.classBuilder(CLASS_TYPE_ADAPTER_FACTORY)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(TypeAdapterFactory.class)
                .addField(factoryListTypeName, "mTypeAdapterFactories", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(constructorSpec);

        MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName), "type")
                .addCode("for (TypeAdapterFactory adapterFactory : mTypeAdapterFactories) {\n" +
                        "\tTypeAdapter<T> typeAdapter = adapterFactory.create(gson, type);\n" +
                        "\tif (typeAdapter != null) {\n" +
                        "\t\treturn typeAdapter;\n" +
                        "\t}\n" +
                        "}\n" +
                        "return null;\n");

        adapterFactoryBuilder.addMethod(createMethodBuilder.build());
        return adapterFactoryBuilder.build();
    }

}
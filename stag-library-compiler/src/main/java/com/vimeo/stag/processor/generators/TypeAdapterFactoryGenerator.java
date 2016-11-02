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
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.generators.model.ClassInfo;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Modifier;

public class TypeAdapterFactoryGenerator {

    @NotNull
    private final ClassInfo mInfo;

    public TypeAdapterFactoryGenerator(@NotNull ClassInfo info) {
        mInfo = info;
    }

    /**
     * Generates the TypeSpec for the TypeAdapterFactory
     * that this class generates.
     *
     * @return a valid TypeSpec that can be written
     * to a file or added to another class.
     */
    @NotNull
    public TypeSpec getTypeAdapterFactorySpec() {
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(mInfo.getTypeAdapterFactoryClassName())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(TypeAdapterFactory.class))
                .addMethod(getCreateMethodSpec());

        return adapterBuilder.build();
    }

    @NotNull
    private MethodSpec getCreateMethodSpec() {
        TypeVariableName genericType = TypeVariableName.get("T");
        AnnotationSpec suppressions = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "\"unchecked\" /* Protected by TypeToken */")
                .build();
        return MethodSpec.methodBuilder("create")
                .addTypeVariable(genericType)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericType), "type")
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericType))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(suppressions)
                .addAnnotation(Override.class)
                .addCode("Class<? super T> clazz = type.getRawType();\n" +
                         "if (clazz == " + mInfo.getClassAndPackage() + ".class) {\n" +
                         "\treturn (TypeAdapter<T>) new " + mInfo.getTypeAdapterQualifiedClassName() +
                         "(gson);\n" +
                         "}\n" +
                         "return null;\n")
                .build();
    }

}

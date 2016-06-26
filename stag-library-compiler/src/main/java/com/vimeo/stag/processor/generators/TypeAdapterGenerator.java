package com.vimeo.stag.processor.generators;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.generators.model.ClassInfo;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import javax.lang.model.element.Modifier;

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
class TypeAdapterGenerator {

    private static final String CLASS_SUFFIX_ADAPTER = "Adapter";

    @NotNull private final ClassInfo mInfo;

    public TypeAdapterGenerator(@NotNull ClassInfo info) {
        mInfo = info;
    }

    /**
     * Generates the TypeSpec for the TypeAdapter
     * that this class generates.
     *
     * @return a valid TypeSpec that can be written
     * to a file or added to another class.
     */
    public TypeSpec getTypeAdapterSpec() {
        TypeName typeVariableName = TypeVariableName.get(mInfo.getType());

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addStatement("mGson = gson")
                .build();

        TypeSpec.Builder innerAdapterBuilder = TypeSpec.classBuilder(mInfo.getClassName() + CLASS_SUFFIX_ADAPTER)
                .addModifiers(Modifier.STATIC)
                .addField(Gson.class, "mGson", Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(constructor)
                .superclass(
                        ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));

        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName);

        innerAdapterBuilder.addMethod(writeMethod);
        innerAdapterBuilder.addMethod(readMethod);

        return innerAdapterBuilder.build();
    }

    private static MethodSpec getWriteMethodSpec(@NotNull TypeName typeName) {
        return MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "out")
                .addParameter(typeName, "value")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class)
                .addCode("ParseUtils.write(mGson, out, value);\n")
                .build();
    }

    private MethodSpec getReadMethodSpec(@NotNull TypeName typeName) {
        return MethodSpec.methodBuilder("read")
                .addParameter(JsonReader.class, "in")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class)
                .addCode("return ParseUtils.parse" + mInfo.getClassName() + "(mGson, in);\n")
                .build();
    }

}

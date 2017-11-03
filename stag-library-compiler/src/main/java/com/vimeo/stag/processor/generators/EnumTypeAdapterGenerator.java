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
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.utils.FileGenUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

public class EnumTypeAdapterGenerator extends AdapterGenerator {

    @NotNull
    private final ClassInfo mInfo;

    @NotNull
    private final TypeElement mElement;

    public EnumTypeAdapterGenerator(@NotNull ClassInfo info, @NotNull TypeElement element) {
        mInfo = info;
        mElement = element;
    }

    @NotNull
    private static MethodSpec getWriteMethodSpec(@NotNull TypeName typeName) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(typeName, "object")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addStatement("writer.value(object == null ? null : CONSTANT_TO_NAME.get(object))");
        return builder.build();
    }

    @NotNull
    private static MethodSpec getReadMethodSpec(@NotNull TypeName typeName) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("read")
                .addParameter(JsonReader.class, "reader")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.beginControlFlow("if (reader.peek() == com.google.gson.stream.JsonToken.NULL)");
        builder.addStatement("reader.nextNull()");
        builder.addStatement("return null");
        builder.endControlFlow();
        builder.addStatement("return NAME_TO_CONSTANT.get(reader.nextString())");
        return builder.build();
    }

    /**
     * Generates the TypeSpec for the TypeAdapter
     * that this enum generates.
     *
     * @return a valid TypeSpec that can be written
     * to a file or added to another class.
     */
    @Override
    @NotNull
    public TypeSpec createTypeAdapterSpec(@NotNull StagGenerator stagGenerator) {
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson");

        String className = FileGenUtils.unescapeEscapedString(mInfo.getTypeAdapterClassName());
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));


        Map<String, Element> nameToConstant = new HashMap<>();
        Map<Element, String> constantToName = new HashMap<>();

        for (Element enclosingElement : mElement.getEnclosedElements()) {
            if (enclosingElement.getKind() == ElementKind.ENUM_CONSTANT) {
                String name = getJsonName(enclosingElement);
                nameToConstant.put(name, enclosingElement);
                constantToName.put(enclosingElement, name);

                String[] alternateJsonNames = getAlternateJsonNames(enclosingElement);
                if (alternateJsonNames != null && alternateJsonNames.length > 0) {
                    for (String alternate : alternateJsonNames) {
                        nameToConstant.put(alternate, enclosingElement);
                    }
                }
            }
        }


        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName);

        adapterBuilder.addField(createTypeTokenSpec(typeMirror));

        TypeName typeName =
                ParameterizedTypeName.get(ClassName.get(HashMap.class), TypeVariableName.get(String.class),
                                          TypeVariableName.get(typeMirror));
        adapterBuilder.addField(typeName, "NAME_TO_CONSTANT", Modifier.PRIVATE, Modifier.STATIC,
                                Modifier.FINAL);

        typeName = ParameterizedTypeName.get(ClassName.get(HashMap.class), TypeVariableName.get(typeMirror),
                                             TypeVariableName.get(String.class));
        adapterBuilder.addField(typeName, "CONSTANT_TO_NAME", Modifier.PRIVATE, Modifier.STATIC,
                                Modifier.FINAL);

        CodeBlock.Builder staticBlockBuilder = CodeBlock.builder();
        staticBlockBuilder.addStatement("NAME_TO_CONSTANT = new HashMap<>(" + nameToConstant.size() + ")");
        for (Map.Entry<String, Element> entry : nameToConstant.entrySet()) {
            staticBlockBuilder.addStatement(
                    "NAME_TO_CONSTANT.put(\"" + entry.getKey() + "\", " + typeVariableName + "." +
                    entry.getValue().getSimpleName().toString() + ")");
        }

        staticBlockBuilder.add("\n");
        staticBlockBuilder.addStatement("CONSTANT_TO_NAME = new HashMap<>(" + constantToName.size() + ")");
        for (Map.Entry<Element, String> entry : constantToName.entrySet()) {
            staticBlockBuilder.addStatement("CONSTANT_TO_NAME.put(" + typeVariableName + "." +
                                            entry.getKey().getSimpleName().toString() + ", \"" +
                                            entry.getValue() + "\")");
        }

        adapterBuilder.addStaticBlock(staticBlockBuilder.build());

        adapterBuilder.addMethod(constructorBuilder.build());
        adapterBuilder.addMethod(writeMethod);
        adapterBuilder.addMethod(readMethod);

        return adapterBuilder.build();
    }
}
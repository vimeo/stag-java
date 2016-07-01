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
package com.vimeo.stag.processor.generators;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

public class StagGenerator {

    public static final String CLASS_STAG = "Stag";
    private static final String CLASS_TYPE_ADAPTER_FACTORY = "Factory";

    @NotNull
    private final Filer mFiler;

    public StagGenerator(@NotNull Filer filer) {
        mFiler = filer;
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

        TypeVariableName genericTypeName = TypeVariableName.get("T");

        adaptersBuilder.addMethod(getWriteToAdapterSpec(genericTypeName));
        adaptersBuilder.addMethod(getReadFromAdapterSpec(genericTypeName));
        adaptersBuilder.addMethod(getWriteListToAdapterSpec(genericTypeName));
        adaptersBuilder.addMethod(getReadListFromAdapterSpec(genericTypeName));

        Set<Element> list = SupportedTypesModel.getInstance().getSupportedElements();

        for (Element element : list) {
            if (TypeUtils.isConcreteType(element)) {
                ClassInfo classInfo = new ClassInfo(element.asType());
                TypeAdapterGenerator typeAdapter = new TypeAdapterGenerator(classInfo);

                adaptersBuilder.addType(typeAdapter.getTypeAdapterSpec());
            }
        }

        adaptersBuilder.addType(getAdapterFactorySpec(list));

        JavaFile javaFile =
                JavaFile.builder(FileGenUtils.GENERATED_PACKAGE_NAME, adaptersBuilder.build()).build();

        FileGenUtils.writeToFile(javaFile, mFiler);
    }

    @NotNull
    private static MethodSpec getWriteToAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("writeToAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addTypeVariable(genericTypeName)
                .addException(IOException.class)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonWriter.class, "out")
                .addParameter(genericTypeName, "value")
                .addCode("gson.getAdapter(clazz).write(out, value);\n")
                .build();
    }

    @NotNull
    private static MethodSpec getReadFromAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("readFromAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(genericTypeName)
                .addTypeVariable(genericTypeName)
                .addException(IOException.class)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonReader.class, "in")
                .addCode("return gson.getAdapter(clazz).read(in);\n")
                .build();
    }

    @NotNull
    private static MethodSpec getWriteListToAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("writeListToAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addTypeVariable(genericTypeName)
                .addException(IOException.class)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonWriter.class, "out")
                .addParameter(ParameterizedTypeName.get(ClassName.get(ArrayList.class), genericTypeName),
                              "list")
                .addCode("com.google.gson.TypeAdapter<T> typeAdapter = gson.getAdapter(clazz);\n" +
                         '\n' +
                         "for (T object : list) {\n" +
                         "\ttypeAdapter.write(out, object);\n" +
                         "}\n")
                .build();
    }

    @NotNull
    private static MethodSpec getReadListFromAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("readListFromAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ArrayList.class), genericTypeName))
                .addTypeVariable(genericTypeName)
                .addException(IOException.class)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonReader.class, "in")
                .addCode("ArrayList<T> list = new java.util.ArrayList<>();\n" +
                         "com.google.gson.TypeAdapter<T> typeAdapter = gson.getAdapter(clazz);\n" +
                         '\n' +
                         "while(in.hasNext()){\n" +
                         "\tlist.add(typeAdapter.read(in));\n" +
                         "}\n" +
                         '\n' +
                         "return list;\n")
                .build();
    }

    @NotNull
    private static TypeSpec getAdapterFactorySpec(Set<Element> types) {
        TypeVariableName genericTypeName = TypeVariableName.get("T");
        TypeSpec.Builder adapterFactoryBuilder = TypeSpec.classBuilder(CLASS_TYPE_ADAPTER_FACTORY)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(TypeAdapterFactory.class);

        StringBuilder factoryReturnBuilder = new StringBuilder(types.size());

        for (Element element : types) {
            if (TypeUtils.isConcreteType(element)) {
                ClassInfo classInfo = new ClassInfo(element.asType());

                factoryReturnBuilder.append("if (clazz == ")
                        .append(classInfo.getClassAndPackage())
                        .append(".class) {\n\treturn (TypeAdapter<T>) new ")
                        .append(classInfo.getClassName())
                        .append("Adapter(gson);\n}\n");
            }
        }

        MethodSpec createTypeAdapterMethod = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName),
                              "type")
                .addCode("Class<? super T> clazz = type.getRawType();\n" +
                         '\n' +
                         factoryReturnBuilder +
                         '\n' + "return null;\n")
                .build();

        adapterFactoryBuilder.addMethod(createTypeAdapterMethod);
        return adapterFactoryBuilder.build();
    }

}
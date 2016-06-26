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

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

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
public class StagGenerator {

    public static final String CLASS_STAG = "Stag";
    private static final String CLASS_TYPE_ADAPTER_FACTORY = "Factory";

    @NotNull private final Filer mFiler;
    @NotNull private final Set<TypeMirror> mTypes;

    public StagGenerator(@NotNull Filer filer, @NotNull Set<TypeMirror> types) {
        mFiler = filer;
        mTypes = types;
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

        for (TypeMirror type : mTypes) {
            ClassInfo classInfo = new ClassInfo(type);
            TypeAdapterGenerator typeAdapter = new TypeAdapterGenerator(classInfo);

            adaptersBuilder.addType(typeAdapter.getTypeAdapterSpec());
        }

        adaptersBuilder.addType(getAdapterFactorySpec(mTypes));

        JavaFile javaFile = JavaFile.builder(FileGenUtils.GENERATED_PACKAGE_NAME, adaptersBuilder.build()).build();

        FileGenUtils.writeToFile(javaFile, mFiler);
    }

    @NotNull
    private static MethodSpec getWriteToAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("writeToAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addTypeVariable(genericTypeName)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonWriter.class, "out")
                .addParameter(genericTypeName, "value")
                .addCode("try {\n" +
                        "\tgson.getAdapter(clazz).write(out, value);\n" +
                        "} catch (IOException e) {\n" +
                        "\te.printStackTrace();\n" +
                        "}\n")
                .build();
    }

    @NotNull
    private static MethodSpec getReadFromAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("readFromAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(genericTypeName)
                .addTypeVariable(genericTypeName)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonReader.class, "in")
                .addCode("try {\n" +
                        "\treturn gson.getAdapter(clazz).read(in);\n" +
                        "} catch (IOException e) {\n" +
                        "\te.printStackTrace();\n" +
                        "}\n" +
                        "return null;\n")
                .build();
    }

    @NotNull
    private static MethodSpec getWriteListToAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("writeListToAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(void.class)
                .addTypeVariable(genericTypeName)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonWriter.class, "out")
                .addParameter(ParameterizedTypeName.get(ClassName.get(ArrayList.class), genericTypeName), "list")
                .addCode("try {\n" +
                        "\tcom.google.gson.TypeAdapter<T> typeAdapter = gson.getAdapter(clazz);\n" +
                        '\n' +
                        "\tfor (T object : list) {\n" +
                        "\t\ttypeAdapter.write(out, object);\n" +
                        "\t}\n" +
                        "} catch (IOException e) {\n" +
                        "\te.printStackTrace();\n" +
                        "}\n")
                .build();
    }

    @NotNull
    private static MethodSpec getReadListFromAdapterSpec(@NotNull TypeVariableName genericTypeName) {
        return MethodSpec.methodBuilder("readListFromAdapter")
                .addModifiers(Modifier.STATIC)
                .returns(ParameterizedTypeName.get(ClassName.get(ArrayList.class), genericTypeName))
                .addTypeVariable(genericTypeName)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addParameter(JsonReader.class, "in")
                .addCode("try {\n" +
                        "\tArrayList<T> list = new java.util.ArrayList<>();\n" +
                        "\tcom.google.gson.TypeAdapter<T> typeAdapter = gson.getAdapter(clazz);\n" +
                        '\n' +
                        "\twhile(in.hasNext()){\n" +
                        "\t\tlist.add(typeAdapter.read(in));\n" +
                        "\t}\n" +
                        '\n' +
                        "\treturn list;\n" +
                        "} catch (IOException e) {\n" +
                        "\te.printStackTrace();\n" +
                        "}\n" +
                        "return null;\n")
                .build();
    }

    @NotNull
    private static TypeSpec getAdapterFactorySpec(Set<TypeMirror> types) {
        TypeVariableName genericTypeName = TypeVariableName.get("T");
        TypeSpec.Builder adapterFactoryBuilder = TypeSpec.classBuilder(CLASS_TYPE_ADAPTER_FACTORY)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(TypeAdapterFactory.class);

        StringBuilder factoryReturnBuilder = new StringBuilder(types.size());

        for (TypeMirror type : types) {

            ClassInfo classInfo = new ClassInfo(type);

            factoryReturnBuilder.append("if (clazz.equals(")
                    .append(classInfo.getClassAndPackage())
                    .append(".class)) {\n\treturn (TypeAdapter<T>) new ")
                    .append(classInfo.getClassName())
                    .append("Adapter(gson);\n}\n");
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
                        '\n' + "return null;")
                .build();

        adapterFactoryBuilder.addMethod(createTypeAdapterMethod);
        return adapterFactoryBuilder.build();
    }

}
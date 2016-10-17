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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.processor.generators.model.AnnotatedClass;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@SuppressWarnings("StringConcatenationMissingWhitespace")
public class TypeAdapterGenerator {

    static final String CLASS_SUFFIX_ADAPTER = "TypeAdapter";

    @NotNull
    private final ClassInfo mInfo;

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
    @NotNull
    public TypeSpec getTypeAdapterSpec() {
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson");

        TypeSpec.Builder adapterBuilder =
                TypeSpec.classBuilder(mInfo.getClassName() + CLASS_SUFFIX_ADAPTER)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class),
                                                              typeVariableName));

        AnnotatedClass annotatedClass = SupportedTypesModel.getInstance().getSupportedType(typeMirror);
        Map<Element, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        addAdapterFields(adapterBuilder, constructorBuilder, memberVariables);
        adapterBuilder.addMethod(constructorBuilder.build());

        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName, memberVariables);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName, memberVariables);

        adapterBuilder.addMethod(writeMethod);
        adapterBuilder.addMethod(readMethod);

        return adapterBuilder.build();
    }

    @NotNull
    private MethodSpec getWriteMethodSpec(
            @NotNull TypeName typeName,
            Map<Element, TypeMirror> memberVariables
    ) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(typeName, "object")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addCode("\tif (object == null) {\n" +
                "\t\treturn;\n" +
                "\t}\n" +
                "\twriter.beginObject();\n");

        for (Map.Entry<Element, TypeMirror> element : memberVariables.entrySet()) {
            String name = getJsonName(element.getKey());
            String variableName = element.getKey().getSimpleName().toString();
            String variableType = element.getValue().toString();

            boolean isPrimitive = isPrimitive(variableType);

            String prefix = isPrimitive ? "\t" : "\t\t";
            if (!isPrimitive) {
                builder.addCode("\tif (object." + variableName + " != null) {\n");
            }
            builder.addCode(getWriteCode(prefix, element.getValue(), name, "object." + variableName));
            if (!isPrimitive) {
                builder.addCode("\t}\n");
            }
        }
        builder.addCode("\twriter.endObject();\n");

        return builder.build();
    }

    @NotNull
    private MethodSpec getReadMethodSpec(TypeName typeName, Map<Element, TypeMirror> elements) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("read")
                .addParameter(JsonReader.class, "reader")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addCode("\tif (reader.peek() == com.google.gson.stream.JsonToken.NULL) {\n" +
                        "\t\treader.nextNull();\n" +
                        "\t\treturn null;\n" +
                        "\t}\n" +
                        "\tif (reader.peek() != com.google.gson.stream.JsonToken.BEGIN_OBJECT) {\n" +
                        "\t\treader.skipValue();\n" +
                        "\t\treturn null;\n" +
                        "\t}\n" +
                        "\treader.beginObject();\n" +
                        '\n' +
                        '\t' + mInfo.getClassAndPackage() + " object = new " + mInfo.getClassAndPackage() +
                        "();\n" +
                        "\twhile (reader.hasNext()) {\n" +
                        "\t\tString name = reader.nextName();\n" +
                        "\t\tcom.google.gson.stream.JsonToken jsonToken = reader.peek();\n" +
                        "\t\tif (jsonToken == com.google.gson.stream.JsonToken.NULL) {\n" +
                        "\t\t\treader.skipValue();\n" +
                        "\t\t\tcontinue;\n" +
                        "\t\t}\n" +
                        "\t\tswitch (name) {\n");

        for (Map.Entry<Element, TypeMirror> element : elements.entrySet()) {
            String name = getJsonName(element.getKey());
            String variableName = element.getKey().getSimpleName().toString();
            String jsonTokenType = getReadTokenType(element.getValue());

            if (jsonTokenType != null) {
                builder.addCode("\t\t\tcase \"" + name + "\":\n" +
                        "\t\t\t\tif (jsonToken == " + jsonTokenType +
                        ") {\n" +
                        getReadCode("\t\t\t\t\t", variableName, element.getValue()) +
                        "\n\t\t\t\t} else {" +
                        "\n\t\t\t\t\treader.skipValue();" +
                        "\n\t\t\t\t}" +
                        '\n' +
                        "\t\t\t\tbreak;\n");
            } else {
                builder.addCode("\t\t\tcase \"" + name + "\":\n" +
                        "\t\t\t\ttry {\n" +
                        getReadCode("\t\t\t\t\t", variableName, element.getValue()) +
                        "\n\t\t\t\t} catch(Exception exception) {" +
                        "\n\t\t\t\t\tthrow new IOException(\"Error parsing " +
                        mInfo.getClassName() + "." + variableName + " JSON!\", exception);" +
                        "\n\t\t\t\t}" +
                        '\n' +
                        "\t\t\t\tbreak;\n");
            }
        }

        builder.addCode("\t\t\tdefault:\n" +
                "\t\t\t\treader.skipValue();\n" +
                "\t\t\t\tbreak;\n" +
                "\t\t}\n" +
                "\t}\n" +
                '\n' +
                "\treader.endObject();\n" +
                "\treturn object;\n");

        return builder.build();
    }

    private void addAdapterFields(TypeSpec.Builder adapterBuilder,
                                  MethodSpec.Builder constructorBuilder,
                                  Map<Element, TypeMirror> memberVariables) {
        HashSet<TypeMirror> typeSet = new HashSet<>(memberVariables.values());
        for (TypeMirror fieldType : typeSet) {
            if (isNative(fieldType.toString()))
                continue;

            if (TypeUtils.getOuterClassType(fieldType).equals(ArrayList.class.getName())) {
                fieldType = getInnerListType(fieldType);
            }

            TypeName typeName = getAdapterFieldTypeName(fieldType);
            String fieldName = getAdapterField(fieldType);

            adapterBuilder.addField(typeName, fieldName, Modifier.PRIVATE, Modifier.FINAL);
            constructorBuilder.addStatement(fieldName + " = gson.getAdapter(" + fieldType + ".class)");
        }
    }

    private TypeName getAdapterFieldTypeName(TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
    }

    private String getAdapterField(TypeMirror type) {
        ClassInfo classInfo = new ClassInfo(type);
        return "m" + classInfo.getClassName() + CLASS_SUFFIX_ADAPTER;
    }


    private static boolean isPrimitive(@NotNull String type) {
        return type.equals(long.class.getName()) ||
                type.equals(double.class.getName()) ||
                type.equals(boolean.class.getName()) ||
                type.equals(int.class.getName());
    }

    private static boolean isNative(@NotNull String type) {
        return isPrimitive(type) ||
                type.equals(String.class.getName());
    }

    private static TypeMirror getInnerListType(@NotNull TypeMirror type) {
        return ((DeclaredType) type).getTypeArguments().get(0);
    }

    @NotNull
    private static String getJsonName(Element element) {
        String name = element.getAnnotation(GsonAdapterKey.class).value();

        if (name == null || name.length() == 0) {
            name = element.getSimpleName().toString();
        }
        return name;
    }

    @Nullable
    private static String getReadTokenType(@NotNull TypeMirror type) {
        if (type.toString().equals(long.class.getName())) {
            return "com.google.gson.stream.JsonToken.NUMBER";
        } else if (type.toString().equals(double.class.getName())) {
            return "com.google.gson.stream.JsonToken.NUMBER";
        } else if (type.toString().equals(boolean.class.getName())) {
            return "com.google.gson.stream.JsonToken.BOOLEAN";
        } else if (type.toString().equals(String.class.getName())) {
            return "com.google.gson.stream.JsonToken.STRING";
        } else if (type.toString().equals(int.class.getName())) {
            return "com.google.gson.stream.JsonToken.NUMBER";
        } else if (TypeUtils.getOuterClassType(type).equals(ArrayList.class.getName())) {
            return "com.google.gson.stream.JsonToken.BEGIN_ARRAY";
        } else {
            return null;
        }

    }

    private String getReadCode(
            String prefix,
            String variableName,
            TypeMirror type) {
        if (TypeUtils.getOuterClassType(type).equals(ArrayList.class.getName())) {
            TypeMirror innerType = getInnerListType(type);
            String innerRead = getAdapterRead(innerType);
            return prefix + "reader.beginArray();\n" +
                    prefix + "object." + variableName + " = new java.util.ArrayList<>();\n" +
                    prefix + "while (reader.hasNext()) {\n" +
                    prefix + "\tobject." + variableName + ".add(" + innerRead + ");\n" +
                    prefix + "}\n" +
                    prefix + "reader.endArray();";
        } else {
            return prefix + "object." + variableName + " = " +
                    getReadType(type);
        }
    }

    @NotNull
    private String getReadType(
            @NotNull TypeMirror type) {
        if (type.toString().equals(long.class.getName())) {
            return "reader.nextLong();";
        } else if (type.toString().equals(double.class.getName())) {
            return "reader.nextDouble();";
        } else if (type.toString().equals(boolean.class.getName())) {
            return "reader.nextBoolean();";
        } else if (type.toString().equals(String.class.getName())) {
            return "reader.nextString();";
        } else if (type.toString().equals(int.class.getName())) {
            return "reader.nextInt();";
        } else {
            return getAdapterRead(type) + ";";
        }
    }

    private String getWriteCode(
            @NotNull String prefix,
            @NotNull TypeMirror type,
            @NotNull String jsonName,
            @NotNull String variableName
    ) {
        if (TypeUtils.getOuterClassType(type).equals(ArrayList.class.getName())) {
            TypeMirror innerType = getInnerListType(type);
            String innerWrite = getAdapterWrite(innerType, "item");
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                    prefix + "writer.beginArray();\n" +
                    prefix + "for (" + innerType + " item : " + variableName + ") {\n" +
                    prefix + "\t" + innerWrite + ";\n" +
                    prefix + "}\n" +
                    prefix + "writer.endArray();\n";
        } else {
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                    prefix + getWriteType(type, variableName) + '\n';

        }
    }


    @NotNull
    private String getWriteType(
            @NotNull TypeMirror type,
            @NotNull String variableName) {
        if (type.toString().equals(long.class.getName()) ||
                type.toString().equals(double.class.getName()) ||
                type.toString().equals(boolean.class.getName()) ||
                type.toString().equals(String.class.getName()) ||
                type.toString().equals(int.class.getName())) {
            return "writer.value(" + variableName + ");";
        } else {
            return getAdapterWrite(type, variableName) + ";";
        }
    }

    private String getAdapterWrite(TypeMirror type, String variableName)
    {
        String adapterField = getAdapterField(type);
        return adapterField + ".write(writer, " + variableName + ")";
    }

    private String getAdapterRead(TypeMirror type)
    {
        String adapterField = getAdapterField(type);
        return adapterField + ".read(reader)";
    }

}

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
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

@SuppressWarnings("StringConcatenationMissingWhitespace")
public class TypeAdapterGenerator {

    private static final String TYPE_ADAPTER_FIELD_PREFIX = "mTypeAdapter";
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
    public TypeSpec getTypeAdapterSpec(TypeTokenConstantsGenerator typeTokenConstantsGenerator) {
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson");

        String className = FileGenUtils.unescapeEscapedString(mInfo.getTypeAdapterClassName());
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));

        AnnotatedClass annotatedClass = SupportedTypesModel.getInstance().getSupportedType(typeMirror);
        Map<Element, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        Map<String, String> adapterFieldMap = addAdapterFields(adapterBuilder, constructorBuilder, memberVariables, typeTokenConstantsGenerator);
        adapterBuilder.addMethod(constructorBuilder.build());

        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName, memberVariables, adapterFieldMap);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName, memberVariables, adapterFieldMap);

        adapterBuilder.addMethod(writeMethod);
        adapterBuilder.addMethod(readMethod);

        return adapterBuilder.build();
    }

    @NotNull
    private static MethodSpec getWriteMethodSpec(@NotNull TypeName typeName,
                                                 @NotNull Map<Element, TypeMirror> memberVariables,
                                                 @NotNull Map<String, String> typeAdapterVariableNames) {
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
            builder.addCode(getWriteCode(prefix, element.getValue(), name, "object." + variableName, typeAdapterVariableNames));
            if (!isPrimitive) {
                builder.addCode("\t}\n");
            }
        }
        builder.addCode("\twriter.endObject();\n");

        return builder.build();
    }

    private static Map<String, String> addAdapterFields(@NotNull TypeSpec.Builder adapterBuilder,
                                                            @NotNull MethodSpec.Builder constructorBuilder,
                                                            @NotNull Map<Element, TypeMirror> memberVariables,
                                                        @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator) {
        HashSet<TypeMirror> typeSet = new HashSet<>(memberVariables.values());
        HashMap<String, String> typeAdapterNamesMap = new HashMap<>(typeSet.size());
        HashSet<TypeMirror> exclusiveTypeSet = new HashSet<>();

        for (TypeMirror fieldType : typeSet) {
            if (isNative(fieldType.toString())) {
                continue;
            }

            if (isArray(fieldType)) {
                fieldType = getInnerListType(fieldType);
            }

            exclusiveTypeSet.add(fieldType);
        }

        for (TypeMirror fieldType : exclusiveTypeSet) {
            TypeName typeName = getAdapterFieldTypeName(fieldType);
            String fieldName = typeAdapterNamesMap.get(fieldType.toString());
            if (null == fieldName) {
                fieldName = TYPE_ADAPTER_FIELD_PREFIX + typeAdapterNamesMap.size();
                typeAdapterNamesMap.put(fieldType.toString(), fieldName);
                String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
                adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
                constructorBuilder.addStatement(fieldName + " = gson.getAdapter(" + typeTokenConstantsGenerator.addTypeToken(fieldType) + ")");
            }
        }

        return typeAdapterNamesMap;
    }

    private static boolean isPrimitive(@NotNull String type) {
        return type.equals(long.class.getName()) ||
                type.equals(double.class.getName()) ||
                type.equals(boolean.class.getName()) ||
                type.equals(float.class.getName()) ||
                type.equals(int.class.getName());
    }

    private static boolean isArray(@NotNull TypeMirror type) {
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(ArrayList.class.getName()) ||
                outerClassType.equals(List.class.getName());
    }

    private static TypeName getAdapterFieldTypeName(@NotNull TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
    }

    @NotNull
    private static String getJsonName(@NotNull Element element) {
        String name = element.getAnnotation(GsonAdapterKey.class).value();

        if (name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        return name;
    }

    private static boolean isNative(@NotNull String type) {
        return isPrimitive(type) || type.equals(String.class.getName());
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
        } else if (type.toString().equals(float.class.getName())) {
            return "com.google.gson.stream.JsonToken.NUMBER";
        } else if (isArray(type)) {
            return "com.google.gson.stream.JsonToken.BEGIN_ARRAY";
        } else {
            return null;
        }
    }

    private static TypeMirror getInnerListType(@NotNull TypeMirror type) {
        return ((DeclaredType) type).getTypeArguments().get(0);
    }

    @NotNull
    private static String getReadCode(@NotNull String prefix, @NotNull String variableName,
                                      @NotNull TypeMirror type, @NotNull Map<String, String> typeAdapterFieldMap) {
        String outerClassType = TypeUtils.getOuterClassType(type);
        if (isArray(type)) {
            TypeMirror innerType = getInnerListType(type);
            String innerRead = getReadType(innerType, typeAdapterFieldMap);
            return prefix + "reader.beginArray();\n" +
                    prefix + "object." + variableName + " = new java.util.ArrayList<>();\n" +
                    prefix + "while (reader.hasNext()) {\n" +
                    prefix + "\tobject." + variableName + ".add(" + innerRead + ");\n" +
                    prefix + "}\n" +
                    prefix + "reader.endArray();";
        } else {
            return prefix + "object." + variableName + " = " +
                    getReadType(type, typeAdapterFieldMap) + ";";
        }
    }

    @NotNull
    private static String getReadType(@NotNull TypeMirror type, @NotNull Map<String, String> typeAdapterFieldMap) {
        if (type.toString().equals(long.class.getName())) {
            return "reader.nextLong()";
        } else if (type.toString().equals(double.class.getName())) {
            return "reader.nextDouble()";
        } else if (type.toString().equals(boolean.class.getName())) {
            return "reader.nextBoolean()";
        } else if (type.toString().equals(String.class.getName())) {
            return "reader.nextString()";
        } else if (type.toString().equals(int.class.getName())) {
            return "reader.nextInt()";
        } else if (type.toString().equals(float.class.getName())) {
            return "(float) reader.nextDouble()";
        } else {
            return getAdapterRead(type, typeAdapterFieldMap);
        }
    }

    private static String getWriteCode(@NotNull String prefix, @NotNull TypeMirror type,
                                       @NotNull String jsonName, @NotNull String variableName,
                                       @NotNull Map<String, String> typeAdapterFieldMap) {
        if (isArray(type)) {
            TypeMirror innerType = getInnerListType(type);
            String innerWrite = getWriteType(innerType, "item", typeAdapterFieldMap);
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                    prefix + "writer.beginArray();\n" +
                    prefix + "for (" + innerType + " item : " + variableName + ") {\n" +
                    prefix + "\t" + innerWrite + ";\n" +
                    prefix + "}\n" +
                    prefix + "writer.endArray();\n";
        } else {
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                    prefix + getWriteType(type, variableName, typeAdapterFieldMap) + '\n';

        }
    }

    @NotNull
    private static String getWriteType(@NotNull TypeMirror type, @NotNull String variableName, @NotNull Map<String, String> typeAdapterFieldMap) {
        if (type.toString().equals(long.class.getName()) ||
                type.toString().equals(double.class.getName()) ||
                type.toString().equals(boolean.class.getName()) ||
                type.toString().equals(String.class.getName()) ||
                type.toString().equals(int.class.getName()) ||
                type.toString().equals(float.class.getName())) {
            return "writer.value(" + variableName + ");";
        } else {
            return getAdapterWrite(type, variableName, typeAdapterFieldMap) + ";";
        }
    }

    private static String getAdapterWrite(@NotNull TypeMirror type, @NotNull String variableName, @NotNull Map<String, String> typeAdapterFieldMap) {
        String adapterField = typeAdapterFieldMap.get(type.toString());
        return adapterField + ".write(writer, " + variableName + ")";
    }

    private static String getAdapterRead(@NotNull TypeMirror type, @NotNull Map<String, String> typeAdapterFieldMap) {
        String adapterField = typeAdapterFieldMap.get(type.toString());
        return adapterField + ".read(reader)";
    }

    @NotNull
    private MethodSpec getReadMethodSpec(@NotNull TypeName typeName,
                                         @NotNull Map<Element, TypeMirror> elements,
                                         @NotNull Map<String, String> typeAdapterFieldMap) {
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
                        getReadCode("\t\t\t\t\t", variableName, element.getValue(), typeAdapterFieldMap) +
                        "\n\t\t\t\t} else {" +
                        "\n\t\t\t\t\treader.skipValue();" +
                        "\n\t\t\t\t}" +
                        '\n' +
                        "\t\t\t\tbreak;\n");
            } else {
                builder.addCode("\t\t\tcase \"" + name + "\":\n" +
                        "\t\t\t\ttry {\n" +
                        getReadCode("\t\t\t\t\t", variableName, element.getValue(), typeAdapterFieldMap) +
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
}
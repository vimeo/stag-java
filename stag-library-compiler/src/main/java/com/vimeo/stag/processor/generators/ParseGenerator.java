package com.vimeo.stag.processor.generators;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.utils.FileGenUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
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
public class ParseGenerator {

    private static final String CLASS_PARSE_UTILS = "ParseUtils";

    @NotNull
    private final Set<String> mSupportedTypes;

    @NotNull
    private final Filer mFiler;

    @NotNull
    private final Map<TypeMirror, List<VariableElement>> mVariableMap;

    public ParseGenerator(@NotNull Set<String> supportedTypes, @NotNull Filer filer,
                          @NotNull Map<TypeMirror, List<VariableElement>> variableMap) {
        mSupportedTypes = supportedTypes;
        mFiler = filer;
        mVariableMap = variableMap;
    }

    private static MethodSpec generateParseArraySpec() {

        TypeVariableName genericTypeName = TypeVariableName.get("T");

        return MethodSpec.methodBuilder("parseArray")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(ArrayList.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(JsonReader.class, "reader")
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addException(IOException.class)
                .addCode("reader.beginArray();\n" +
                         '\n' +
                         "ArrayList<" + genericTypeName.name + "> list = " + StagGenerator.CLASS_STAG +
                         ".readListFromAdapter(gson, clazz, reader);\n" +
                         '\n' +
                         "reader.endArray();\n" +
                         "return list;\n")
                .build();
    }

    private static MethodSpec generateWriteArraySpec() {
        TypeVariableName genericType = TypeVariableName.get("T");
        return MethodSpec.methodBuilder("write")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addTypeVariable(genericType)
                .addException(IOException.class)
                .addParameter(Gson.class, "gson")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(Class.class, "clazz")
                .addParameter(ParameterizedTypeName.get(ClassName.get(ArrayList.class), genericType), "list")
                .addCode("if (list == null) {\n" +
                         "\treturn;\n" +
                         "}\n" +
                         "writer.beginArray();\n" +
                         '\n' +
                         "Stag.writeListToAdapter(gson, clazz, writer, list);\n" +
                         '\n' +
                         "writer.endArray();\n")
                .build();
    }

    /**
     * Generates the ParseUtils class. This class includes
     * parsing/write methods for JsonArray -> ArrayList, and
     * the parsing/write methods for all objects supported by
     * the Stag library.
     *
     * @throws IOException thrown if we are unable to write
     *                     to the source file for ParseUtils. Most likely the
     *                     file is being held by another process, barring us from
     *                     modifying it.
     */
    public void generateParsingCode() throws IOException {
        TypeSpec.Builder typeSpecBuilder =
                TypeSpec.classBuilder(CLASS_PARSE_UTILS).addModifiers(Modifier.FINAL);

        typeSpecBuilder.addMethod(ParseGenerator.generateParseArraySpec());
        typeSpecBuilder.addMethod(ParseGenerator.generateWriteArraySpec());

        for (Map.Entry<TypeMirror, List<VariableElement>> entry : mVariableMap.entrySet()) {
            TypeMirror type = entry.getKey();
            List<VariableElement> elements = entry.getValue();

            typeSpecBuilder.addMethod(generateWriteSpec(type, elements));
            typeSpecBuilder.addMethod(generateParseSpec(type, elements));
        }

        JavaFile javaFile =
                JavaFile.builder(FileGenUtils.GENERATED_PACKAGE_NAME, typeSpecBuilder.build()).build();

        FileGenUtils.writeToFile(javaFile, mFiler);
    }

    private MethodSpec generateWriteSpec(TypeMirror type, List<VariableElement> elements) {
        MethodSpec.Builder writeBuilder = MethodSpec.methodBuilder("write")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(Gson.class, "gson")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(ClassName.get(type), "object")
                .addException(IOException.class)
                .returns(void.class)
                .addCode("\twriter.beginObject();\n" +
                         "\tif (object == null) {\n" +
                         "\t\treturn;\n" +
                         "\t} else {\n");

        for (VariableElement element : elements) {
            String name = getJsonName(element);
            String variableName = element.getSimpleName().toString();
            String variableType = element.asType().toString();

            boolean isPrimitive = isPrimitive(variableType);

            if (!isPrimitive) {
                writeBuilder.addCode("\t\tif (object." + variableName + " != null) {\n");
            }
            writeBuilder.addCode("\t\t\twriter.name(\"" + name + "\");\n");
            writeBuilder.addCode("\t\t\t" + getWriteType(element.asType(), variableName) + '\n');
            if (!isPrimitive) {
                writeBuilder.addCode("\t\t}\n");
            }
        }
        writeBuilder.addCode("\t}\n" + "\twriter.endObject();\n");

        return writeBuilder.build();
    }

    private static String getJsonName(VariableElement element) {
        String name = element.getAnnotation(GsonAdapterKey.class).value();

        if (name == null || name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        return name;
    }

    private MethodSpec generateParseSpec(TypeMirror type, List<VariableElement> elements) {
        ClassInfo info = new ClassInfo(type);

        MethodSpec.Builder parseBuilder = MethodSpec.methodBuilder("parse" + info.getClassName())
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(ClassName.get(info.getType()))
                .addParameter(Gson.class, "gson")
                .addParameter(JsonReader.class, "reader")
                .addException(IOException.class)
                .addCode("\treader.beginObject();\n" +
                         '\n' +
                         '\t' + info.getClassAndPackage() + " object = new " + info.getClassAndPackage() +
                         "();\n" +
                         "\twhile (reader.hasNext()) {\n" +
                         "\t\tString name = reader.nextName();\n" +
                         "\t\tcom.google.gson.stream.JsonToken jsonToken = reader.peek();\n" +
                         "\t\tif (jsonToken == com.google.gson.stream.JsonToken.NULL) {\n" +
                         "\t\t\treader.skipValue();\n" +
                         "\t\t\tcontinue;\n" +
                         "\t\t}\n" +
//                        "java.lang.System.out.println(jsonToken.toString());" +
                         "\t\tswitch (name) {\n");

        for (VariableElement element : elements) {
            String name = getJsonName(element);

            String variableName = element.getSimpleName().toString();

//            String variableType = element.asType().toString();
//            if (variableType.contains("List")) {
//                debugPrintTypes(element.asType());
//            }

            parseBuilder.addCode("\t\t\tcase \"" + name + "\":\n" +
                                 "\t\t\t\tobject." + variableName + " = " + getReadType(element.asType()) +
                                 '\n' +
                                 "\t\t\t\tbreak;\n");
        }

        parseBuilder.addCode("\t\t\tdefault:\n" +
                             "\t\t\t\treader.skipValue();\n" +
                             "\t\t\t\tbreak;\n" +
                             "\t\t}\n" +
                             "\t}\n" +
                             '\n' +
                             "\treader.endObject();\n" +
                             "\treturn object;\n");

        return parseBuilder.build();
    }

    private String getReadType(TypeMirror type) {
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
        } else if (getOuterClassType(type).equals(ArrayList.class.getName())) {
            return "ParseUtils.parseArray(gson, reader, " + getInnerListType(type).toString() + ".class);";
        } else {
            String typeName = type.toString();
            if (!mSupportedTypes.contains(type.toString())) {
                return StagGenerator.CLASS_STAG + ".readFromAdapter(gson, " + typeName + ".class, reader);";
            } else {
                ClassInfo info = new ClassInfo(type);
                return "ParseUtils.parse" + info.getClassName() + "(gson, reader);";
            }
        }
    }

    private String getWriteType(TypeMirror type, String variableName) {
        if (type.toString().equals(long.class.getName()) ||
            type.toString().equals(double.class.getName()) ||
            type.toString().equals(boolean.class.getName()) ||
            type.toString().equals(String.class.getName()) ||
            type.toString().equals(int.class.getName())) {
            return "writer.value(object." + variableName + ");";
        } else if (getOuterClassType(type).equals(ArrayList.class.getName())) {
            return "ParseUtils.write(gson, writer, " + getInnerListType(type).toString() + ".class, object." +
                   variableName + ");";
        } else {
            if (!mSupportedTypes.contains(type.toString())) {
                return StagGenerator.CLASS_STAG + ".writeToAdapter(gson, " + type +
                       ".class, writer, object." +
                       variableName + ");";
            } else {
                return "ParseUtils.write(gson, writer, object." + variableName + ");";
            }
        }
    }

    private static boolean isPrimitive(String type) {
        return type.equals(long.class.getName()) ||
               type.equals(double.class.getName()) ||
               type.equals(boolean.class.getName()) ||
               type.equals(int.class.getName());
    }

    private static TypeMirror getInnerListType(TypeMirror type) {
        return ((DeclaredType) type).getTypeArguments().get(0);
    }

    private static String getOuterClassType(TypeMirror type) {
        if (type instanceof DeclaredType) {
            return ((DeclaredType) type).asElement().toString();
        } else {
            return type.toString();
        }
    }

}

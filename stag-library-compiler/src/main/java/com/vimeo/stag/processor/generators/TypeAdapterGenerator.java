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
import com.sun.org.apache.regexp.internal.RE;
import com.vimeo.stag.WriteRuntimeType;
import com.vimeo.stag.processor.generators.model.AnnotatedClass;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.KnownTypeAdapterUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

public class TypeAdapterGenerator extends AdapterGenerator {

    private static final String TYPE_ADAPTER_FIELD_PREFIX = "mTypeAdapter";
    private static boolean mGsonVariableUsed;
    private static boolean mStagFactoryUsed;

    @NotNull
    private final ClassInfo mInfo;

    public TypeAdapterGenerator(@NotNull ClassInfo info) {
        mInfo = info;
    }

    @Nullable
    private static String getTypeTokenCode(@NotNull TypeMirror fieldType,
                                           @NotNull Map<TypeVariable, String> typeVarsMap,
                                           @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator) {
        String result = null;
        if (!TypeUtils.isConcreteType(fieldType)) {
            if (fieldType.getKind() == TypeKind.TYPEVAR) {
                result = typeVarsMap.get(fieldType);
            } else if (fieldType instanceof DeclaredType) {
                /*
                 * If it is of ParameterizedType, {@link com.vimeo.stag.utils.ParameterizedTypeUtil} is used to get the
                 * type token of the parameter type.
                 */
                DeclaredType declaredFieldType = (DeclaredType) fieldType;
                List<? extends TypeMirror> typeMirrors = ((DeclaredType) fieldType).getTypeArguments();
                if (isMap(fieldType)) {
                    TypeMirror keyTypeMirror = typeMirrors.get(0);
                    TypeMirror valueTypeMirror = typeMirrors.get(1);
                    if (isSupportedNative(keyTypeMirror.toString())) {
                        if (isMap(valueTypeMirror)) {
                            result = getTypeTokenCode(valueTypeMirror, typeVarsMap, typeTokenConstantsGenerator);
                        } else {
                            result = typeVarsMap.get(valueTypeMirror);
                        }
                        result = "new com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter(" + KnownTypeAdapterUtils.getKnownTypeAdapterForType(keyTypeMirror.toString())
                                + ", " + result + ", null)";
                    } else {
                        result = "com.google.gson.reflect.TypeToken.getParameterized(" +
                                declaredFieldType.asElement().toString() + ".class";
                    }
                } else if (isArray(fieldType)) {
                    TypeMirror valueTypeMirror = typeMirrors.get(0);
                    if (isArray(valueTypeMirror)) {
                        result = getTypeTokenCode(valueTypeMirror, typeVarsMap, typeTokenConstantsGenerator);
                    } else {
                        result = typeVarsMap.get(valueTypeMirror);
                    }
                    result = "new com.vimeo.stag.KnownTypeAdapters.ListTypeAdapter(" + result + ", null)";
                }
            }
        } else {
            result = typeTokenConstantsGenerator.addTypeToken(fieldType);
        }

        return result;
    }

    static boolean isSupportedPrimitive(@NotNull String type) {
        return type.equals(long.class.getName())
                || type.equals(double.class.getName())
                || type.equals(boolean.class.getName())
                || type.equals(float.class.getName())
                || type.equals(int.class.getName())
                || type.equals(char.class.getName())
                || type.equals(short.class.getName())
                || type.equals(byte.class.getName());
    }

    private static boolean isNativeArray(@NotNull TypeMirror type) {
        return (type instanceof ArrayType);
    }

    static boolean isArray(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        if (isNativeArray(type)) {
            return true;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(ArrayList.class.getName()) ||
                outerClassType.equals(List.class.getName()) ||
                outerClassType.equals(Collection.class.getName());
    }

    static boolean isMap(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(Map.class.getName()) ||
                outerClassType.equals(HashMap.class.getName()) ||
                outerClassType.equals(LinkedHashMap.class.getName());
    }

    @NotNull
    private static TypeName getAdapterFieldTypeName(@NotNull TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
    }

    static boolean isSupportedNative(@NotNull String type) {
        return isSupportedPrimitive(type)
                || type.equals(String.class.getName())
                || type.equals(Long.class.getName())
                || type.equals(Integer.class.getName())
                || type.equals(Boolean.class.getName())
                || type.equals(Double.class.getName())
                || type.equals(Float.class.getName())
                || type.equals(Number.class.getName());
    }

    /**
     * Check if the type is one of the numbers
     */
    private static boolean isNumberType(@NotNull String typeString) {
        return typeString.equals(long.class.getName())
                || typeString.equals(Long.class.getName())
                || typeString.equals(double.class.getName())
                || typeString.equals(Double.class.getName())
                || typeString.equals(int.class.getName())
                || typeString.equals(Integer.class.getName())
                || typeString.equals(float.class.getName())
                || typeString.equals(Float.class.getName());
    }


    @NotNull
    private static TypeMirror getArrayInnerType(@NotNull TypeMirror type) {
        return (type instanceof ArrayType) ? ((ArrayType) type).getComponentType() : ((DeclaredType) type).getTypeArguments().get(0);
    }

    @NotNull
    private static String getArrayListType(@NotNull TypeMirror innerArrayType) {
        String innerArrayTypeString = innerArrayType.toString();
        if (innerArrayTypeString.equals(long.class.getName())) {
            return Long.class.getName();
        }
        if (innerArrayTypeString.equals(double.class.getName())) {
            return Double.class.getName();
        }
        if (innerArrayTypeString.equals(Boolean.class.getName())) {
            return Boolean.class.getName();
        }
        if (innerArrayTypeString.equals(float.class.getName())) {
            return Float.class.getName();
        }
        if (innerArrayTypeString.equals(int.class.getName())) {
            return Integer.class.getName();
        } else {
            return innerArrayType.toString();
        }
    }

    @NotNull
    private static String getReadCode(@NotNull String prefix, @NotNull String variableName,
                                      @NotNull TypeMirror type, @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (isNativeArray(type)) {
            TypeMirror innerType = getArrayInnerType(type);
            String innerRead = getReadType(type, innerType, adapterFieldInfo);
            String arrayListVariableName = "tmp" + variableName;
            String stagGetterName = adapterFieldInfo.getKnownAdapterStagFunctionCalls(innerType);
            String result = prefix + "reader.beginArray();\n" +
                    prefix + "java.util.ArrayList<" + getArrayListType(innerType) + "> " + arrayListVariableName + " = new java.util.ArrayList<>();\n" +
                    (stagGetterName != null ? prefix + "TypeAdapter<" + innerType + "> adapter = " + stagGetterName + ";\n" : "") +
                    prefix + "while (reader.hasNext()) {\n" +
                    prefix + "\t" + arrayListVariableName + ".add(" + innerRead + ");\n" +
                    prefix + "}\n" +
                    prefix + "reader.endArray();\n";

            result += prefix + "object." + variableName + "= new " + innerType.toString() + "[" + arrayListVariableName + ".size()];\n";
            result += prefix + "for(int idx = 0; idx < " + arrayListVariableName + ".size(); idx++) {\n";
            result += prefix + "\tobject." + variableName + "[idx] = " + arrayListVariableName + ".get(idx);\n";
            result += prefix + "}\n";

            return result;
        } else if (isArray(type)) {
            TypeMirror innerType = getArrayInnerType(type);
            String adapter = adapterFieldInfo.getAdapter(innerType);
            if (isSupportedNative(innerType.toString())) {
                mStagFactoryUsed = true;
                adapter = "" + KnownTypeAdapterUtils.getKnownTypeAdapterForType(innerType.toString());
            }

            String listInstantiater = KnownTypeAdapterUtils.getListInstantiater(type);
            return prefix + "object." + variableName + " = new com.vimeo.stag.KnownTypeAdapters.ListTypeAdapter<" + innerType + "," + type + ">(" + adapter + ", new " + listInstantiater + "()).read(reader);";
        } else {
            if (isMap(type) && type instanceof DeclaredType) {
                List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
                if (typeArguments.size() == 2 && isSupportedNative(typeArguments.get(0).toString())) {
                    TypeMirror valueTypeMirror = typeArguments.get(1);

                    mStagFactoryUsed = true;
                    String keyAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(typeArguments.get(0).toString());
                    String valueAdapter = adapterFieldInfo.getAdapter(valueTypeMirror);

                    if (valueAdapter == null || isSupportedNative(valueAdapter)) {
                        valueAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(typeArguments.get(1).toString());
                    }

                    String mapInstantiater = KnownTypeAdapterUtils.getMapInstantiater(type);
                    return prefix + "object." + variableName + " = new com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter<" + typeArguments.get(0) + "," + typeArguments.get(1) + "," + type + ">(" + keyAdapter + ", " + valueAdapter + ", new " + mapInstantiater + "()).read(reader);";
                }
            }
            return prefix + "object." + variableName + " = " +
                    getReadType(type, type, adapterFieldInfo) + ";";
        }
    }

    @NotNull
    private static String getReadType(@NotNull TypeMirror parentType, @NotNull TypeMirror type,
                                      @NotNull AdapterFieldInfo adapterFieldInfo) {
        String typeString = type.toString();
        String adapterAccessor = KnownTypeAdapterUtils.getKnownTypeAdapterForType(typeString);
        return (null != adapterAccessor)
                ? adapterAccessor + ".read(reader)"
                : getAdapterRead(parentType, type, adapterFieldInfo);
    }

    @NotNull
    private static String getAdapterRead(@NotNull TypeMirror parentType, @NotNull TypeMirror type,
                                         @NotNull AdapterFieldInfo adapterFieldInfo) {
        String adapterCode;
        if (adapterFieldInfo.getKnownAdapterStagFunctionCalls(type) != null && isArray(parentType)) {
            adapterCode = "adapter.read(reader)";
        } else {
            adapterCode = adapterFieldInfo.getAdapter(type) + ".read(reader)";
        }
        return adapterCode;
    }

    @NotNull
    private static MethodSpec getReadMethodSpec(@NotNull TypeName typeName,
                                                @NotNull Map<Element, TypeMirror> elements,
                                                @NotNull AdapterFieldInfo adapterFieldInfo) {
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
                '\t' + typeName + " object = new " + typeName +
                "();\n" +
                "\twhile (reader.hasNext()) {\n" +
                "\t\tString name = reader.nextName();\n" +
                "\t\tcom.google.gson.stream.JsonToken jsonToken = reader.peek();\n" +
                "\t\tif (jsonToken == com.google.gson.stream.JsonToken.NULL) {\n" +
                "\t\t\treader.skipValue();\n" +
                "\t\t\tcontinue;\n" +
                "\t\t}\n" +
                "\t\tswitch (name) {\n");

        final List<String> nonNullFields = new ArrayList<>();

        for (Map.Entry<Element, TypeMirror> element : elements.entrySet()) {
            String name = getJsonName(element.getKey());
            final String variableName = element.getKey().getSimpleName().toString();
            final TypeMirror elementValue = element.getValue();

            builder.addCode("\t\t\tcase \"" + name + "\":\n" +
                    "\t\t\t\tobject." + variableName + " = " + adapterFieldInfo.getAdapter(elementValue) + ".read(reader);");

            builder.addCode("\n\t\t\t\tbreak;\n");
            runIfAnnotationSupported(element.getKey().getAnnotationMirrors(), new Runnable() {
                @Override
                public void run() {
                    if(!isSupportedPrimitive(elementValue.toString())) {
                        nonNullFields.add(variableName);
                    }
                }
            });
        }

        builder.addCode("\t\t\tdefault:\n" +
                "\t\t\t\treader.skipValue();\n" +
                "\t\t\t\tbreak;\n" +
                "\t\t}\n" +
                "\t}\n" +
                '\n' +
                "\treader.endObject();\n");

        for (String nonNullField : nonNullFields) {
            builder.addCode("\n\tif (object." + nonNullField + " == null) {");
            builder.addCode("\n\t\tthrow new java.io.IOException(\"" + nonNullField + " cannot be null\");");
            builder.addCode("\n\t}\n\n");
        }

        builder.addCode("\treturn object;\n");

        return builder.build();
    }

    private static void runIfAnnotationSupported(@NotNull List<? extends AnnotationMirror> annotationMirrors,
                                                 @NotNull Runnable runnable) {
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            switch (annotationMirror.toString()) {
                case "@javax.validation.constraints.NotNull":
                case "@edu.umd.cs.findbugs.annotations.NonNull":
                case "@javax.annotation.Nonnull":
                case "@lombok.NonNull":
                case "@org.eclipse.jdt.annotation.NonNull":
                case "@org.jetbrains.annotations.NotNull":
                case "@android.support.annotation.NonNull":
                    runnable.run();
                    break;
            }
        }
    }

    private String getAdapterAccessor(@NotNull TypeMirror fieldType,
                                      @NotNull TypeSpec.Builder adapterBuilder,
                                      @NotNull MethodSpec.Builder constructorBuilder,
                                      @NotNull Map<Element, TypeMirror> memberVariables,
                                      @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                      @NotNull Map<TypeVariable, String> typeVarsMap,
                                      @NotNull StagGenerator stagGenerator,
                                      @NotNull AdapterFieldInfo adapterFieldInfo) {
        String knownTypeAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(fieldType.toString());
        if (null != knownTypeAdapter) {
            return knownTypeAdapter;
        } else {
            String getterField = stagGenerator.getClassAdapterFactoryMethod(fieldType);
            if(null != getterField) {
                mGsonVariableUsed = true;
                mStagFactoryUsed = true;
                /*
                 * If we already have the adapter generated for the fieldType in Stag.Factory class
                 */
                return "mStagFactory.get" + getterField + "(mGson)";
            } else if (TypeUtils.isConcreteType(fieldType)) {
                getterField = stagGenerator.addFieldType(fieldType);
                mGsonVariableUsed = true;
                mStagFactoryUsed = true;
                return "mStagFactory.get" + getterField + "(mGson)";
            } else {
                String fieldName = adapterFieldInfo.getFieldName(fieldType);
                if (null == fieldName) {
                    fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
                    adapterFieldInfo.addField(fieldType, fieldName);
                    String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
                    TypeName typeName = getAdapterFieldTypeName(fieldType);
                    adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
                    constructorBuilder.addStatement(
                            fieldName + " = " + getTypeTokenCode(fieldType, typeVarsMap, typeTokenConstantsGenerator));
                }
                return fieldName;
            }
        }
    }

    @NotNull
    private AdapterFieldInfo addAdapterFields(@NotNull TypeSpec.Builder adapterBuilder,
                                              @NotNull MethodSpec.Builder constructorBuilder,
                                              @NotNull Map<Element, TypeMirror> memberVariables,
                                              @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                              @NotNull Map<TypeVariable, String> typeVarsMap,
                                              @NotNull StagGenerator stagGenerator) {

        HashSet<TypeMirror> typeSet = new HashSet<>(memberVariables.values());
        AdapterFieldInfo result = new AdapterFieldInfo(typeSet.size());
        HashSet<TypeMirror> exclusiveTypeSet = new HashSet<>();

        for (TypeMirror fieldType : typeSet) {
            if (isArray(fieldType)) {
                exclusiveTypeSet.add(getArrayInnerType(fieldType));
            } else if (isMap(fieldType) && fieldType instanceof DeclaredType) {
                List<? extends TypeMirror> typeArguments = ((DeclaredType) fieldType).getTypeArguments();
                if (typeArguments.size() == 2) {
                    exclusiveTypeSet.add(typeArguments.get(0));
                    exclusiveTypeSet.add(typeArguments.get(1));
                } else {
                    exclusiveTypeSet.add(fieldType);
                }
            } else {
                exclusiveTypeSet.add(fieldType);
            }
        }

        for (TypeMirror fieldType : exclusiveTypeSet) {
            String knownTypeAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(fieldType.toString());
            if (null != knownTypeAdapter) {
                result.addTypeToAdapterAccessor(fieldType.toString(),
                        knownTypeAdapter);
            } else  {
                String getterField = stagGenerator.getClassAdapterFactoryMethod(fieldType);
                if(null != getterField) {

                /*
                 * If we already have the adapter generated for the fieldType in Stag.Factory class
                 */
                    result.addTypeToAdapterAccessor(fieldType.toString(),
                            "mStagFactory.get" + getterField + "(mGson)");
                    mGsonVariableUsed = true;
                    mStagFactoryUsed = true;
                } else if (TypeUtils.isConcreteType(fieldType)) {
                    getterField = stagGenerator.addFieldType(fieldType);
                    result.addTypeToAdapterAccessor(fieldType.toString(),
                            "mStagFactory.get" + getterField + "(mGson)");
                    mGsonVariableUsed = true;
                    mStagFactoryUsed = true;
                } else {

                }
            }

        }

        return result;
    }

    @NotNull
    private MethodSpec getWriteMethodSpec(@NotNull TypeName typeName,
                                          @NotNull Map<Element, TypeMirror> memberVariables,
                                          @NotNull AdapterFieldInfo adapterFieldInfo) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(typeName, "object")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addStatement("writer.beginObject()");
        builder.beginControlFlow("if (object == null)");
        builder.addStatement("writer.endObject()");
        builder.addStatement("return");
        builder.endControlFlow();

        for (Map.Entry<Element, TypeMirror> element : memberVariables.entrySet()) {
            String name = getJsonName(element.getKey());
            final String variableName = element.getKey().getSimpleName().toString();
            String variableType = element.getValue().toString();

            boolean isPrimitive = isSupportedPrimitive(variableType);

            builder.addCode("\n");
            if (!isPrimitive) {
                builder.beginControlFlow("if (object." + variableName + " != null) ");
            }

            builder.addStatement("writer.name(\""+ name + "\")");
            if (!isPrimitive) {
                builder.addStatement(adapterFieldInfo.getAdapter(element.getValue()) + ".write(writer, object." + variableName + ")");
                /*
                * If the element is annotated with NonNull annotation, throw {@link IOException} if it is null.
                */
                runIfAnnotationSupported(element.getKey().getAnnotationMirrors(), new Runnable() {
                    @Override
                    public void run() {
                        builder.endControlFlow();
                        builder.beginControlFlow("else if (object." + variableName + " == null)");
                        builder.addStatement("throw new java.io.IOException(\"" + variableName +
                                " cannot be null\")");
                    }
                });

                builder.endControlFlow();
            } else {
                builder.addStatement("writer.value(object." +  variableName + ")");
            }
        }

        builder.addCode("\n");
        builder.addStatement("writer.endObject()");
        return builder.build();
    }

    /**
     * Generates the TypeSpec for the TypeAdapter
     * that this class generates.
     *
     * @return a valid TypeSpec that can be written
     * to a file or added to another class.
     */
    @Override
    @NotNull
    public TypeSpec getTypeAdapterSpec(@NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                       @NotNull StagGenerator stagGenerator) {
        mGsonVariableUsed = false;
        mStagFactoryUsed = false;
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        List<? extends TypeMirror> typeArguments = mInfo.getTypeArguments();

        TypeVariableName stagFactoryTypeName = stagGenerator.getGeneratedClassName();
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .addParameter(stagFactoryTypeName, "stagFactory");

        String className = FileGenUtils.unescapeEscapedString(mInfo.getTypeAdapterClassName());
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));

        Map<TypeVariable, String> typeVarsMap = new HashMap<>();

        int idx = 0;
        if (null != typeArguments) {
            for (TypeMirror innerTypeMirror : typeArguments) {
                if (innerTypeMirror.getKind() == TypeKind.TYPEVAR) {
                    TypeVariable typeVariable = (TypeVariable) innerTypeMirror;
                    String simpleName = typeVariable.asElement().getSimpleName().toString();
                    adapterBuilder.addTypeVariable(TypeVariableName.get(simpleName, TypeVariableName.get(typeVariable.getUpperBound())));
                    ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), TypeVariableName.get(innerTypeMirror.toString()));
                    String paramName = "typeAdapter" + idx;
                    constructorBuilder.addParameter(parameterizedTypeName, paramName);
                    typeVarsMap.put(typeVariable, paramName);
                    idx++;
                }
            }
        }

        AnnotatedClass annotatedClass = SupportedTypesModel.getInstance().getSupportedType(typeMirror);
        Map<Element, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        AdapterFieldInfo adapterFieldInfo = addAdapterFields(adapterBuilder, constructorBuilder, memberVariables,
                typeTokenConstantsGenerator, typeVarsMap, stagGenerator);

        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);

        if (mGsonVariableUsed) {
            adapterBuilder.addField(Gson.class, "mGson", Modifier.FINAL, Modifier.PRIVATE);
            constructorBuilder.addStatement("this.mGson = gson");
        }

        if (mStagFactoryUsed) {
            adapterBuilder.addField(stagFactoryTypeName, "mStagFactory", Modifier.FINAL, Modifier.PRIVATE);
            constructorBuilder.addStatement("this.mStagFactory = stagFactory");
        }

        adapterBuilder.addMethod(constructorBuilder.build());
        adapterBuilder.addMethod(writeMethod);
        adapterBuilder.addMethod(readMethod);

        return adapterBuilder.build();
    }

    @NotNull
    private String getWriteCode(@NotNull Element key, @NotNull String prefix, @NotNull TypeMirror type,
                                @NotNull String jsonName, @NotNull String variableName,
                                @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (isNativeArray(type)) {
            TypeMirror innerType = getArrayInnerType(type);
            String innerWrite = getWriteType(key, innerType, "item", adapterFieldInfo);
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                    prefix + "writer.beginArray();\n" +
                    prefix + "for (" + innerType + " item : " + variableName + ") {\n" +
                    prefix + "\t" + innerWrite + "\n" +
                    prefix + "}\n" +
                    prefix + "writer.endArray();\n";
        } else if (isArray(type)) {
            TypeMirror innerType = getArrayInnerType(type);
            String adapter = adapterFieldInfo.getAdapter(innerType);
            if (isSupportedNative(innerType.toString())) {
                mStagFactoryUsed = true;
                adapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(innerType.toString());
            }

            String listInstantiater = KnownTypeAdapterUtils.getListInstantiater(type);
            return prefix + "writer.name(\"" + jsonName + "\");\n" +
                    prefix + "new com.vimeo.stag.KnownTypeAdapters.ListTypeAdapter(" + adapter + ", new " + listInstantiater + "()).write(writer, " + variableName + ");\n";
        } else {
            if (isMap(type) && type instanceof DeclaredType) {
                List<? extends TypeMirror> typeArguments = ((DeclaredType) type).getTypeArguments();
                if (typeArguments.size() == 2 && isSupportedNative(typeArguments.get(0).toString())) {
                    TypeMirror keyTypeMirror = typeArguments.get(0);
                    mStagFactoryUsed = true;
                    String keyAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(keyTypeMirror.toString());
                    String valueAdapter = adapterFieldInfo.getAdapter(typeArguments.get(1));
                    if (isSupportedNative(typeArguments.get(1).toString())) {
                        valueAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(typeArguments.get(1).toString());
                    }
                    String mapInstantiater = KnownTypeAdapterUtils.getMapInstantiater(type);
                    return prefix + "writer.name(\"" + jsonName + "\");\n" +
                            prefix + "new com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter(" + keyAdapter + ", " + valueAdapter + ", new " + mapInstantiater + "()).write(writer, " + variableName + ");\n";
                }
            }
            return prefix + "writer.name(\"" + jsonName + "\");\n" + prefix + getWriteType(key, type, variableName, adapterFieldInfo) + '\n';
        }
    }

    @NotNull
    private String getWriteType(@NotNull Element key, @NotNull TypeMirror type, @NotNull String variableName,
                                @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (isSupportedNative(type.toString())) {
            return "writer.value(" + variableName + ");";
        } else {
            return getAdapterWrite(key, type, variableName, adapterFieldInfo) + ";";
        }
    }

    @NotNull
    private String getAdapterWrite(@NotNull Element key, @NotNull TypeMirror type, @NotNull String variableName,
                                   @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (key.getAnnotation(WriteRuntimeType.class) != null) {
            mGsonVariableUsed = true;
            return "((TypeAdapter) mGson.getAdapter(" + variableName + ".getClass())).write(writer, " + variableName + ")";
        } else {
            String adapterField = adapterFieldInfo.getAdapter(type);
            return adapterField + ".write(writer, " + variableName + ")";
        }
    }

    private static class AdapterFieldInfo {

        @NotNull
        private final Map<String, String> mAdapterFields;

        @Nullable
        private Map<String, String> mKnownAdapterStagFunctionCalls;

        AdapterFieldInfo(int capacity) {
            mAdapterFields = new HashMap<>(capacity);
        }

        /**
         * Used to get the stag adapter for a typeMirror if it is already generated in Stag.Factory
         */
        @Nullable
        String getKnownAdapterStagFunctionCalls(@NotNull TypeMirror typeMirror) {
            return mKnownAdapterStagFunctionCalls != null ? mKnownAdapterStagFunctionCalls.get(
                    typeMirror.toString()) : null;
        }

        /**
         * Add the getter method name against a field name
         */
        void addTypeToAdapterAccessor(@NotNull String name, @NotNull String functionName) {
            if (null == mKnownAdapterStagFunctionCalls) {
                mKnownAdapterStagFunctionCalls = new HashMap<>();
            }
            mKnownAdapterStagFunctionCalls.put(name, functionName);
        }

        String getAdapter(@NotNull TypeMirror typeMirror) {
            String typeName = typeMirror.toString();
            String result = null != mKnownAdapterStagFunctionCalls ? mKnownAdapterStagFunctionCalls.get(
                    typeName) : null;
            if (null == result) {
                result = mAdapterFields.get(typeName);
            }
            return result;
        }

        String getFieldName(@NotNull TypeMirror fieldType) {
            return mAdapterFields.get(fieldType.toString());
        }

        int size() {
            return mAdapterFields.size();
        }

        void addField(@NotNull TypeMirror fieldType, @NotNull String fieldName) {
            mAdapterFields.put(fieldType.toString(), fieldName);
        }
    }
}
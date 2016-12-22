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
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private static String getTypeTokenCodeForUnknownTypes(@NotNull TypeMirror fieldType,
                                                          @NotNull Map<TypeVariable, String> typeVarsMap,
                                                          @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator) {
        String result = null;
        if (!TypeUtils.isConcreteType(fieldType)) {
            if (fieldType.getKind() == TypeKind.TYPEVAR) {
                result = " com.google.gson.reflect.TypeToken.get(" + typeVarsMap.get(fieldType) + ")";
            } else if (fieldType instanceof DeclaredType) {
                /**
                 * If it is of ParameterizedType, {@link com.vimeo.stag.utils.ParameterizedTypeUtil} is used to get the
                 * type token of the parameter type.
                 */
                DeclaredType declaredFieldType = (DeclaredType) fieldType;
                List<? extends TypeMirror> typeMirrors = ((DeclaredType) fieldType).getTypeArguments();
                result = "com.google.gson.reflect.TypeToken.getParameterized(" +
                        declaredFieldType.asElement().toString() + ".class";
                /**
                 * Iterate through all the types from the typeArguments and generate type token code accordingly
                 */
                for (TypeMirror parameterTypeMirror : typeMirrors) {
                    if (isSupportedNative(parameterTypeMirror.toString())) {
                        result += ", " + parameterTypeMirror.toString() + ".class";
                    } else if (parameterTypeMirror.getKind() == TypeKind.TYPEVAR) {
                        result += ", " + typeVarsMap.get(parameterTypeMirror);
                    } else {
                        result += ",\n" + getTypeTokenCodeForUnknownTypes(parameterTypeMirror, typeVarsMap,
                                typeTokenConstantsGenerator) + ".getType()";
                    }
                }
                result += ")";
            }
        } else {
            result = typeTokenConstantsGenerator.addTypeToken(fieldType);
        }

        return result;
    }


    @Nullable
    private static String getTypeTokenCode(@NotNull TypeMirror fieldType, @NotNull Map<TypeVariable, String> typeVarsMap,
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
                            /*
                             * If it is a nested map, cases where the value is also a map, recursively call the function to
                             * gets its type adapter
                             */
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
                        /*
                         * If it is a nested list, cases where the value is also a List, recursively call the function to
                         * gets its type adapter
                         */
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

    private static boolean isNativeObject(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(Object.class.getName());
    }

    static boolean isMap(@Nullable TypeMirror type) {
        if (type == null) {
            return false;
        }
        String outerClassType = TypeUtils.getOuterClassType(type);
        return outerClassType.equals(Map.class.getName()) ||
                outerClassType.equals(HashMap.class.getName()) ||
                outerClassType.equals(ConcurrentHashMap.class.getName()) ||
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

    @NotNull
    private static TypeMirror getArrayInnerType(@NotNull TypeMirror type) {
        return (type instanceof ArrayType) ? ((ArrayType) type).getComponentType() : ((DeclaredType) type).getTypeArguments().get(0);
    }

    @NotNull
    private static MethodSpec getReadMethodSpec(@NotNull TypeName typeName, @NotNull Map<Element, TypeMirror> elements, @NotNull AdapterFieldInfo adapterFieldInfo) {
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
                    "\t\t\t\tobject." + variableName + " = " + adapterFieldInfo.getAdapterAccessor(elementValue) + ".read(reader);");

            builder.addCode("\n\t\t\t\tbreak;\n");
            runIfAnnotationSupported(element.getKey().getAnnotationMirrors(), new Runnable() {
                @Override
                public void run() {
                    if (!isSupportedPrimitive(elementValue.toString())) {
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

    private static void runIfAnnotationSupported(@NotNull List<? extends AnnotationMirror> annotationMirrors, @NotNull Runnable runnable) {
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

    private String getAdapterForUnknownGenericType(@NotNull TypeMirror fieldType, @NotNull TypeSpec.Builder adapterBuilder,
                                                   @NotNull MethodSpec.Builder constructorBuilder,
                                                   @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator, @NotNull Map<TypeVariable, String> typeVarsMap,
                                                   @NotNull AdapterFieldInfo adapterFieldInfo) {

        String fieldName = adapterFieldInfo.getFieldName(fieldType);
        if (null == fieldName) {
            fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
            adapterFieldInfo.addField(fieldType, fieldName);
            String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
            TypeName typeName = getAdapterFieldTypeName(fieldType);
            adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
            constructorBuilder.addStatement(fieldName + " = (TypeAdapter<" + fieldType + ">) gson.getAdapter(" + getTypeTokenCodeForUnknownTypes(fieldType, typeVarsMap, typeTokenConstantsGenerator) + ")");
        }
        return fieldName;
    }

    private String getAdapterAccessor(@NotNull TypeMirror fieldType, @NotNull TypeSpec.Builder adapterBuilder,
                                      @NotNull MethodSpec.Builder constructorBuilder,
                                      @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator, @NotNull Map<TypeVariable, String> typeVarsMap,
                                      @NotNull StagGenerator stagGenerator, @NotNull AdapterFieldInfo adapterFieldInfo) {

        String knownTypeAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(fieldType.toString());

        if (null != knownTypeAdapter) {
            return knownTypeAdapter;
        } else if (TypeUtils.isConcreteType(fieldType)) {
            /*
             * If the fields are of concrete types, and not parameterized
             */
            String getterField = stagGenerator.getClassAdapterFactoryMethod(fieldType);

            if (null != getterField) {
                mGsonVariableUsed = true;
                mStagFactoryUsed = true;
                /*
                 * If we already have the adapter generated for the fieldType in Stag.Factory class
                 */
                return "mStagFactory.get" + getterField + "(mGson)";
            } else if (isNativeArray(fieldType)) {
                /*
                 * If the fieldType is of type native arrays such as String[] or int[]
                 */
                TypeMirror arrayInnerType = getArrayInnerType(fieldType);
                if (isSupportedPrimitive(arrayInnerType.toString())) {
                    return KnownTypeAdapterUtils.getNativePrimitiveArrayTypeAdapter(fieldType);
                } else {
                    mStagFactoryUsed = true;
                    mGsonVariableUsed = true;
                    ArrayType arrayType = (ArrayType) fieldType;
                    String adapterAccessor = getAdapterAccessor(arrayInnerType, adapterBuilder, constructorBuilder,
                            typeTokenConstantsGenerator, typeVarsMap, stagGenerator, adapterFieldInfo);
                    String nativeArrayInstantiater = KnownTypeAdapterUtils.getNativeArrayInstantiater(arrayInnerType);
                    String adapterCode = "new com.vimeo.stag.KnownTypeAdapters.ArrayTypeAdapter<" + arrayInnerType.toString() + ">" +
                            "(" + adapterAccessor + ", " + nativeArrayInstantiater + ")";
                    if (arrayType.getComponentType().getKind() != TypeKind.TYPEVAR) {
                        String getterName = stagGenerator.addConcreteFieldType(fieldType, adapterCode.replaceAll("mStagFactory.", "").replaceAll("mGson", "gson"));
                        return "mStagFactory." + getterName + "(mGson)";
                    } else {
                        return adapterCode;
                    }
                }
            } else if (isArray(fieldType)) {
                DeclaredType declaredType = (DeclaredType) fieldType;
                /*
                 * If the fieldType is of type List
                 */
                mStagFactoryUsed = true;
                mGsonVariableUsed = true;
                TypeMirror param = declaredType.getTypeArguments().get(0);
                String paramAdapterAccessor = getAdapterAccessor(param, adapterBuilder, constructorBuilder,
                        typeTokenConstantsGenerator, typeVarsMap, stagGenerator, adapterFieldInfo);
                String listInstantiater = KnownTypeAdapterUtils.getListInstantiater(fieldType);
                String adapterCode = "new com.vimeo.stag.KnownTypeAdapters.ListTypeAdapter<" + param.toString() + "," + fieldType.toString() + ">" +
                        "(" + paramAdapterAccessor + ", " + listInstantiater + ")";
                if (declaredType.getKind() != TypeKind.TYPEVAR) {
                    String getterName = stagGenerator.addConcreteFieldType(fieldType, adapterCode.replaceAll("mStagFactory.", "").replaceAll("mGson", "gson"));
                    return "mStagFactory." + getterName + "(mGson)";
                } else {
                    return adapterCode;
                }
            } else if (isMap(fieldType)) {
                DeclaredType declaredType = (DeclaredType) fieldType;

                 /*
                  * If the fieldType is of type Map
                  */
                mGsonVariableUsed = true;
                mStagFactoryUsed = true;
                TypeMirror keyType = declaredType.getTypeArguments().get(0);
                TypeMirror valueType = declaredType.getTypeArguments().get(1);
                String keyAdapterAccessor = getAdapterAccessor(keyType, adapterBuilder, constructorBuilder,
                        typeTokenConstantsGenerator, typeVarsMap, stagGenerator, adapterFieldInfo);
                String valueAdapterAccessor = getAdapterAccessor(valueType, adapterBuilder, constructorBuilder,
                        typeTokenConstantsGenerator, typeVarsMap, stagGenerator, adapterFieldInfo);
                String mapInstantiater = KnownTypeAdapterUtils.getMapInstantiater(fieldType);
                String adapterCode = "new com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter<" + keyType.toString() + "," + valueType.toString() + "," + fieldType.toString() + ">" +
                        "(" + keyAdapterAccessor + ", " + valueAdapterAccessor + ", " + mapInstantiater + ")";
                if (declaredType.getKind() != TypeKind.TYPEVAR) {
                    String getterName = stagGenerator.addConcreteFieldType(fieldType, adapterCode.replaceAll("mStagFactory.", "").replaceAll("mGson", "gson"));
                    return "mStagFactory." + getterName + "(mGson)";
                } else {
                    return adapterCode;
                }
            } else if (isNativeObject(fieldType)) {
                mGsonVariableUsed = true;
                String adapterCode = "new com.vimeo.stag.KnownTypeAdapters.ObjectTypeAdapter(mGson)";
                String getterName = stagGenerator.addConcreteFieldType(fieldType, adapterCode.replaceAll("mStagFactory.", "").replaceAll("mGson", "gson"));
                return "mStagFactory." + getterName + "(mGson)";
            } else if (fieldType instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) fieldType;
                TypeMirror outerClass = declaredType.asElement().asType();
                String outerClassString = outerClass.toString();
                int idx = outerClassString.indexOf("<");
                if (idx > 0) {
                    outerClassString = outerClassString.substring(0, idx);
                }
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
                String adapterCode = "new " + outerClassString + FileGenUtils.unescapeEscapedString("$LTypeAdapter") + "(gson, this, ";
                for (TypeMirror typeMirror : typeArguments) {
                    adapterCode += getAdapterAccessor(typeMirror, adapterBuilder, constructorBuilder, typeTokenConstantsGenerator, typeVarsMap
                            , stagGenerator, adapterFieldInfo);
                }
                adapterCode += ")";
                return "mStagFactory." + stagGenerator.addGenericFieldType(fieldType, adapterCode.replaceAll("mStagFactory.", "").replaceAll("mGson", "gson")) + "(mGson)";
            } else {
                getterField = stagGenerator.addFieldType(fieldType);
                mGsonVariableUsed = true;
                mStagFactoryUsed = true;
                return "mStagFactory." + "get" + getterField + "(mGson)";
            }
        } else {
            /*
             * If the fieldType is parameterized, generate the typeadapter in the constructor itself.
             */
            String fieldName = adapterFieldInfo.getFieldName(fieldType);
            if (null == fieldName) {
                fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
                adapterFieldInfo.addField(fieldType, fieldName);
                String originalFieldName = FileGenUtils.unescapeEscapedString(fieldName);
                TypeName typeName = getAdapterFieldTypeName(fieldType);
                adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
                constructorBuilder.addStatement(fieldName + " = " + getTypeTokenCode(fieldType, typeVarsMap, typeTokenConstantsGenerator));
            }
            return fieldName;
        }
    }

    @NotNull
    private AdapterFieldInfo addAdapterFields(StagGenerator.GenericClassInfo genericClassInfo, @NotNull TypeSpec.Builder adapterBuilder, @NotNull MethodSpec.Builder constructorBuilder,
                                              @NotNull Map<Element, TypeMirror> memberVariables, @NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                              @NotNull Map<TypeVariable, String> typeVarsMap, @NotNull StagGenerator stagGenerator) {

        HashSet<TypeMirror> typeSet = new HashSet<>(memberVariables.values());
        AdapterFieldInfo result = new AdapterFieldInfo(typeSet.size());
        boolean hasUnkownGenericField = genericClassInfo != null && genericClassInfo.mHasUnknownVarTypeFields;
        for (TypeMirror fieldType : typeSet) {
            String adapterAccessor;
            if (hasUnkownGenericField && TypeUtils.containsTypeVarParams(fieldType)) {
                adapterAccessor = getAdapterForUnknownGenericType(fieldType, adapterBuilder, constructorBuilder, typeTokenConstantsGenerator, typeVarsMap, result);
            } else {
                adapterAccessor = getAdapterAccessor(fieldType, adapterBuilder, constructorBuilder, typeTokenConstantsGenerator, typeVarsMap, stagGenerator, result);
            }

            if (null != adapterAccessor) {
                result.addTypeToAdapterAccessor(fieldType, adapterAccessor);
            }
        }
        return result;
    }

    @NotNull
    private MethodSpec getWriteMethodSpec(@NotNull TypeName typeName, @NotNull Map<Element, TypeMirror> memberVariables, @NotNull AdapterFieldInfo adapterFieldInfo) {
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

            builder.addStatement("writer.name(\"" + name + "\")");
            if (!isPrimitive) {
                WriteRuntimeType annotation = element.getKey().getAnnotation(WriteRuntimeType.class);
                if (annotation != null) {
                    builder.addStatement("((TypeAdapter) mGson.getAdapter(object." + variableName + ".getClass())).write(writer, object." + variableName + ")");
                } else {
                    builder.addStatement(adapterFieldInfo.getAdapterAccessor(element.getValue()) + ".write(writer, object." + variableName + ")");
                }
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
                builder.addStatement("writer.value(object." + variableName + ")");
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
    public TypeSpec getTypeAdapterSpec(@NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator, @NotNull StagGenerator stagGenerator) {
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
        StagGenerator.GenericClassInfo genericClassInfo = null;
        if (null != typeArguments) {
            genericClassInfo = stagGenerator.getGenericClassInfo(mInfo.getType());
            for (TypeMirror innerTypeMirror : typeArguments) {
                if (innerTypeMirror.getKind() == TypeKind.TYPEVAR) {
                    TypeVariable typeVariable = (TypeVariable) innerTypeMirror;
                    String simpleName = typeVariable.asElement().getSimpleName().toString();
                    adapterBuilder.addTypeVariable(TypeVariableName.get(simpleName, TypeVariableName.get(typeVariable.getUpperBound())));
                    String paramName;
                    if (genericClassInfo.mHasUnknownVarTypeFields) {
                        paramName = "type[" + String.valueOf(idx) + "]";
                    } else {
                        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), TypeVariableName.get(innerTypeMirror.toString()));
                        paramName = "typeAdapter" + idx;
                        constructorBuilder.addParameter(parameterizedTypeName, paramName);
                    }

                    typeVarsMap.put(typeVariable, paramName);
                    idx++;
                }
            }

            if (idx > 0 && genericClassInfo.mHasUnknownVarTypeFields) {
                constructorBuilder.addParameter(Type[].class, "type");
                constructorBuilder.varargs(true);
            }
        }

        AnnotatedClass annotatedClass = SupportedTypesModel.getInstance().getSupportedType(typeMirror);
        Map<Element, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        AdapterFieldInfo adapterFieldInfo = addAdapterFields(genericClassInfo, adapterBuilder, constructorBuilder, memberVariables, typeTokenConstantsGenerator, typeVarsMap, stagGenerator);

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

    private static class AdapterFieldInfo {

        //Type.toString -> Accessor Map
        @NotNull
        private final Map<String, String> mAdapterAccessor;

        //Type.toString -> Accessor Map
        @NotNull
        private final Map<String, String> mAdapterFields;

        AdapterFieldInfo(int capacity) {
            mAdapterFields = new HashMap<>(capacity);
            mAdapterAccessor = new HashMap<>(capacity);
        }

        String getAdapterAccessor(@NotNull TypeMirror typeMirror) {
            return mAdapterAccessor.get(typeMirror.toString());
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

        void addTypeToAdapterAccessor(@NotNull TypeMirror typeMirror, String accessorCode) {
            mAdapterAccessor.put(typeMirror.toString(), accessorCode);
        }
    }
}
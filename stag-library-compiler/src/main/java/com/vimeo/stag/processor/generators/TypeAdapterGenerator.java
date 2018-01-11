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
import com.google.gson.internal.bind.TreeTypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.KnownTypeAdapters;
import com.vimeo.stag.KnownTypeAdapters.ArrayTypeAdapter;
import com.vimeo.stag.processor.generators.model.AnnotatedClass;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.generators.model.accessor.FieldAccessor;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.KnownTypeAdapterUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

public class TypeAdapterGenerator extends AdapterGenerator {

    private static final String TYPE_ADAPTER_FIELD_PREFIX = "mTypeAdapter";

    @NotNull
    private final ClassInfo mInfo;
    @NotNull
    private final SupportedTypesModel mSupportedTypesModel;

    public TypeAdapterGenerator(@NotNull SupportedTypesModel supportedTypesModel, @NotNull ClassInfo info) {
        mSupportedTypesModel = supportedTypesModel;
        mInfo = info;
    }

    /**
     * This is used to generate the type token code for the types that are unknown.
     */
    @Nullable
    private static String getTypeTokenCode(@NotNull TypeMirror fieldType,
                                           @NotNull StagGenerator stagGenerator,
                                           @NotNull Map<TypeMirror, String> typeVarsMap,
                                           @NotNull AdapterFieldInfo adapterFieldInfo) {
        if (fieldType.getKind() == TypeKind.TYPEVAR) {
            return adapterFieldInfo.updateAndGetTypeTokenFieldName(fieldType, "(com.google.gson.reflect.TypeToken<" + fieldType.toString() + ">) com.google.gson.reflect.TypeToken.get(" + typeVarsMap.get(fieldType) + ")");
        } else if (!TypeUtils.isParameterizedType(fieldType)) {
            ClassInfo classInfo = stagGenerator.getKnownClass(fieldType);
            if (classInfo != null) {
                return classInfo.getTypeAdapterQualifiedClassName() + ".TYPE_TOKEN";
            } else {
                return adapterFieldInfo.updateAndGetTypeTokenFieldName(fieldType, "com.google.gson.reflect.TypeToken.get(" + fieldType.toString() + ".class)");
            }
        } else if (fieldType instanceof DeclaredType) {
                /*
                 * If it is of ParameterizedType, {@link com.vimeo.stag.utils.ParameterizedTypeUtil} is used to get the
                 * type token of the parameter type.
                 */
            DeclaredType declaredFieldType = (DeclaredType) fieldType;
            List<? extends TypeMirror> typeMirrors = ((DeclaredType) fieldType).getTypeArguments();
            String result = "(com.google.gson.reflect.TypeToken<" + fieldType.toString() + ">)com.google.gson.reflect.TypeToken.getParameterized(" +
                    declaredFieldType.asElement().toString() + ".class";
                /*
                 * Iterate through all the types from the typeArguments and generate type token code accordingly
                 */
            for (TypeMirror parameterTypeMirror : typeMirrors) {
                if (parameterTypeMirror.getKind() != TypeKind.TYPEVAR && !TypeUtils.isParameterizedType(parameterTypeMirror)) {
                    // Optimize so that we do not have to call TypeToken.getType()
                    // When the class is non parametrized and we can call xxxxx.class directly
                    result += ", " + parameterTypeMirror.toString() + ".class";
                } else {
                    result += ", " + getTypeTokenCode(parameterTypeMirror, stagGenerator, typeVarsMap, adapterFieldInfo) + ".getType()";
                }

            }
            result += ")";
            return adapterFieldInfo.updateAndGetTypeTokenFieldName(fieldType, result);
        } else {
            return adapterFieldInfo.updateAndGetTypeTokenFieldName(fieldType, "com.google.gson.reflect.TypeToken.get(" + fieldType.toString() + ".class)");
        }
    }

    @NotNull
    private static TypeName getAdapterFieldTypeName(@NotNull TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
    }

    @NotNull
    private static TypeName getTypeTokenFieldTypeName(@NotNull TypeMirror type) {
        TypeName typeName = TypeVariableName.get(type);
        return ParameterizedTypeName.get(ClassName.get(TypeToken.class), typeName);
    }

    @NotNull
    private static MethodSpec getReadMethodSpec(@NotNull TypeName typeName,
                                                @NotNull Map<FieldAccessor, TypeMirror> elements,
                                                @NotNull AdapterFieldInfo adapterFieldInfo) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("read")
                .addParameter(JsonReader.class, "reader")
                .returns(typeName)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.addStatement("com.google.gson.stream.JsonToken peek = reader.peek()");

        builder.beginControlFlow("if (com.google.gson.stream.JsonToken.NULL == peek)");
        builder.addStatement("reader.nextNull()");
        builder.addStatement("return null");
        builder.endControlFlow();


        builder.beginControlFlow("if (com.google.gson.stream.JsonToken.BEGIN_OBJECT != peek)");
        builder.addStatement("reader.skipValue()");
        builder.addStatement("return null");
        builder.endControlFlow();

        builder.addStatement("reader.beginObject()");
        builder.addStatement(typeName + " object = new " + typeName + "()");

        builder.beginControlFlow("while (reader.hasNext())");
        builder.addStatement("String name = reader.nextName()");
        builder.beginControlFlow("switch (name)");


        final List<FieldAccessor> nonNullFields = new ArrayList<>();

        for (Map.Entry<FieldAccessor, TypeMirror> element : elements.entrySet()) {
            final FieldAccessor fieldAccessor = element.getKey();
            String name = fieldAccessor.getJsonName();

            final TypeMirror elementValue = element.getValue();

            builder.addCode("case \"" + name + "\":\n");

            String[] alternateJsonNames = fieldAccessor.getAlternateJsonNames();
            if (alternateJsonNames != null && alternateJsonNames.length > 0) {
                for (String alternateJsonName : alternateJsonNames) {
                    builder.addCode("case \"" + alternateJsonName + "\":\n");
                }
            }

            String variableType = element.getValue().toString();
            boolean isPrimitive = TypeUtils.isSupportedPrimitive(variableType);

            if (isPrimitive) {
                builder.addStatement("\tobject." +
                        fieldAccessor.createSetterCode(adapterFieldInfo.getAdapterAccessor(elementValue, name) +
                                ".read(reader, object." + fieldAccessor.createGetterCode() + ")"));

            } else {
                builder.addStatement("\tobject." + fieldAccessor.createSetterCode(adapterFieldInfo.getAdapterAccessor(elementValue, name) +
                        ".read(reader)"));
            }


            builder.addStatement("\tbreak");
            if (fieldAccessor.doesRequireNotNull()) {
                if (!TypeUtils.isSupportedPrimitive(elementValue.toString())) {
                    nonNullFields.add(fieldAccessor);
                }
            }
        }

        builder.addCode("default:\n");
        builder.addStatement("reader.skipValue()");
        builder.addStatement("break");
        builder.endControlFlow();
        builder.endControlFlow();

        builder.addStatement("reader.endObject()");

        for (FieldAccessor nonNullField : nonNullFields) {
            builder.beginControlFlow("if (object." + nonNullField.createGetterCode() + " == null)");
            builder.addStatement("throw new java.io.IOException(\"" + nonNullField.createGetterCode() + " cannot be null\")");
            builder.endControlFlow();
        }

        builder.addStatement("return object");

        return builder.build();
    }

    @NotNull
    private static String getInitializationCodeForKnownJsonAdapterType(@NotNull ExecutableElement adapterType,
                                                                       @NotNull StagGenerator stagGenerator,
                                                                       @NotNull Map<TypeMirror, String> typeVarsMap,
                                                                       @NotNull MethodSpec.Builder constructorBuilder,
                                                                       @NotNull TypeMirror fieldType,
                                                                       @NotNull TypeUtils.JsonAdapterType jsonAdapterType,
                                                                       @NotNull AdapterFieldInfo adapterFieldInfo,
                                                                       boolean isNullSafe,
                                                                       @NotNull String keyFieldName) {
        String fieldAdapterAccessor = "new " + FileGenUtils.escapeStringForCodeBlock(adapterType.getEnclosingElement().toString());
        if (jsonAdapterType == TypeUtils.JsonAdapterType.TYPE_ADAPTER) {
            ArrayList<String> constructorParameters = new ArrayList<>();
            if (!adapterType.getParameters().isEmpty()) {
                for (VariableElement parameter : adapterType.getParameters()) {
                    if (parameter.asType().toString().equals(TypeUtils.className(Gson.class))) {
                        constructorParameters.add("gson");
                    } else {
                        throw new IllegalStateException("Not supported " + parameter.asType() + "parameter for @JsonAdapter value");
                    }
                }
            }


            String constructorParameterStr = "(";
            for (int i = 0; i < constructorParameters.size(); i++) {
                constructorParameterStr += constructorParameters.get(i);
                if (i != constructorParameters.size() - 1) {
                    constructorParameterStr += ",";
                }
            }
            constructorParameterStr += ")";
            fieldAdapterAccessor += constructorParameterStr;
        } else if (jsonAdapterType == TypeUtils.JsonAdapterType.TYPE_ADAPTER_FACTORY) {
            String typeTokenAccessorCode = getTypeTokenCode(fieldType, stagGenerator, typeVarsMap, adapterFieldInfo);
            fieldAdapterAccessor += "().create(gson, " + typeTokenAccessorCode + ")";
        } else if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER
                || jsonAdapterType == TypeUtils.JsonAdapterType.JSON_DESERIALIZER
                || jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER_DESERIALIZER) {
            String serializer = null, deserializer = null;

            if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER_DESERIALIZER) {
                String varName = keyFieldName + "SerializerDeserializer";
                String initializer = adapterType.getEnclosingElement().toString() + " " + varName + " = " +
                        "new " + adapterType;
                constructorBuilder.addStatement(initializer);
                serializer = deserializer = varName;
            } else if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_SERIALIZER) {
                serializer = "new " + adapterType;
            } else if (jsonAdapterType == TypeUtils.JsonAdapterType.JSON_DESERIALIZER) {
                deserializer = "new " + adapterType;
            }
            String typeTokenAccessorCode = getTypeTokenCode(fieldType, stagGenerator, typeVarsMap, adapterFieldInfo);
            fieldAdapterAccessor = "new " + TypeVariableName.get(TreeTypeAdapter.class) + "(" + serializer + ", " + deserializer + ", gson, " + typeTokenAccessorCode + ", null)";
        } else {
            throw new IllegalArgumentException(
                    "@JsonAdapter value must be TypeAdapter, TypeAdapterFactory, "
                            + "JsonSerializer or JsonDeserializer reference.");
        }
        String adapterCode = getCleanedFieldInitializer(fieldAdapterAccessor);
        if (isNullSafe) {
            adapterCode += ".nullSafe()";
        }
        return adapterCode;
    }

    private static String getCleanedFieldInitializer(String code) {
        return code.replace("mStagFactory", "stagFactory").replace("mGson", "gson");
    }

    /**
     * Returns the adapter code for the unknown types.
     */
    private static String getAdapterForUnknownGenericType(@NotNull TypeMirror fieldType,
                                                          @NotNull StagGenerator stagGenerator,
                                                          @NotNull Map<TypeMirror, String> typeVarsMap,
                                                          @NotNull AdapterFieldInfo adapterFieldInfo) {

        String fieldName = adapterFieldInfo.getFieldName(fieldType);
        if (null == fieldName) {
            fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
            String fieldInitializationCode = "gson.getAdapter(" +
                    getTypeTokenCode(fieldType, stagGenerator, typeVarsMap, adapterFieldInfo) + ")";
            adapterFieldInfo.addField(fieldType, fieldName, fieldInitializationCode);
        }
        return fieldName;
    }

    @NotNull
    private static MethodSpec getWriteMethodSpec(@NotNull TypeName typeName,
                                                 @NotNull Map<FieldAccessor, TypeMirror> memberVariables,
                                                 @NotNull AdapterFieldInfo adapterFieldInfo) {
        final MethodSpec.Builder builder = MethodSpec.methodBuilder("write")
                .addParameter(JsonWriter.class, "writer")
                .addParameter(typeName, "object")
                .returns(void.class)
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addException(IOException.class);

        builder.beginControlFlow("if (object == null)");
        builder.addStatement("writer.nullValue()");
        builder.addStatement("return");
        builder.endControlFlow();
        builder.addStatement("writer.beginObject()");

        for (Map.Entry<FieldAccessor, TypeMirror> element : memberVariables.entrySet()) {
            FieldAccessor fieldAccessor = element.getKey();
            final String getterCode = fieldAccessor.createGetterCode();

            String name = fieldAccessor.getJsonName();
            String variableType = element.getValue().toString();

            boolean isPrimitive = TypeUtils.isSupportedPrimitive(variableType);

            builder.addCode("\n");
            builder.addStatement("writer.name(\"" + name + "\")");

            if (!isPrimitive) {
                builder.beginControlFlow("if (object." + getterCode + " != null) ");
            }

            if (!isPrimitive) {
                builder.addStatement(
                        adapterFieldInfo.getAdapterAccessor(element.getValue(), name) + ".write(writer, object." +
                                getterCode + ")");
                /*
                * If the element is annotated with NonNull annotation, throw {@link IOException} if it is null.
                */
                builder.endControlFlow();
                builder.beginControlFlow("else");
                if (fieldAccessor.doesRequireNotNull()) {
                    //throw exception in case the field is annotated as NonNull
                    builder.addStatement("throw new java.io.IOException(\"" + getterCode + " cannot be null\")");
                } else {
                    //write null value to the writer if the field is null
                    builder.addStatement("writer.nullValue()");
                }
                builder.endControlFlow();
            } else {
                builder.addStatement("writer.value(object." + getterCode + ")");
            }
        }

        builder.addCode("\n");
        builder.addStatement("writer.endObject()");
        return builder.build();
    }

    /**
     * Returns the adapter code for the known types.
     */
    private String getAdapterAccessor(@NotNull TypeMirror fieldType
            , @NotNull StagGenerator stagGenerator, @NotNull Map<TypeMirror, String> typeVarsMap,
                                      @NotNull AdapterFieldInfo adapterFieldInfo) {

        String knownTypeAdapter = KnownTypeAdapterUtils.getKnownTypeAdapterForType(fieldType);

        if (null != knownTypeAdapter) {
            return knownTypeAdapter;
        }

        String fieldName = adapterFieldInfo.getFieldName(fieldType);
        if (null != fieldName) {
            return fieldName;
        }

        if (TypeUtils.isNativeArray(fieldType)) {
                /*
                 * If the fieldType is of type native arrays such as String[] or int[]
                 */
            TypeMirror arrayInnerType = TypeUtils.getArrayInnerType(fieldType);
            if (TypeUtils.isSupportedPrimitive(arrayInnerType.toString())) {
                return KnownTypeAdapterUtils.getNativePrimitiveArrayTypeAdapter(fieldType);
            } else {
                String adapterAccessor = getAdapterAccessor(arrayInnerType, stagGenerator, typeVarsMap,
                        adapterFieldInfo);
                String nativeArrayInstantiator =
                        KnownTypeAdapterUtils.getNativeArrayInstantiator(arrayInnerType);
                String adapterCode = "new " + TypeUtils.className(ArrayTypeAdapter.class) + "<" +
                        arrayInnerType.toString() + ">" +
                        "(" + adapterAccessor + ", " + nativeArrayInstantiator + ")";
                return adapterCode;
            }
        } else if (TypeUtils.isSupportedList(fieldType)) {
            DeclaredType declaredType = (DeclaredType) fieldType;
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            TypeMirror param = typeArguments.get(0);
            String paramAdapterAccessor = getAdapterAccessor(param, stagGenerator, typeVarsMap, adapterFieldInfo);
            String listInstantiator = KnownTypeAdapterUtils.getListInstantiator(fieldType);
            String adapterCode =
                    "new " + TypeUtils.className(KnownTypeAdapters.ListTypeAdapter.class) + "<" + param.toString() + "," +
                            fieldType.toString() + ">" +
                            "(" + paramAdapterAccessor + ", " + listInstantiator + ")";
            fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
            adapterFieldInfo.addField(fieldType, fieldName, adapterCode);
            return fieldName;

        } else if (TypeUtils.isSupportedMap(fieldType)) {
            DeclaredType declaredType = (DeclaredType) fieldType;
            String mapInstantiator = KnownTypeAdapterUtils.getMapInstantiator(fieldType);
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            String keyAdapterAccessor;
            String valueAdapterAccessor;
            String arguments = "";
            if (typeArguments != null && typeArguments.size() == 2) {
                TypeMirror keyType = typeArguments.get(0);
                TypeMirror valueType = typeArguments.get(1);
                keyAdapterAccessor = getAdapterAccessor(keyType, stagGenerator, typeVarsMap, adapterFieldInfo);
                valueAdapterAccessor = getAdapterAccessor(valueType, stagGenerator, typeVarsMap, adapterFieldInfo);
                arguments = "<" + keyType.toString() + ", " + valueType.toString() + ", " +
                        fieldType.toString() + ">";
            } else {
                // If the map does not have any type arguments, use Object as type params in this case
                keyAdapterAccessor = "mGson.getAdapter(KnownTypeAdapters.ObjectTypeAdapter.TYPE_TOKEN)";
                valueAdapterAccessor = keyAdapterAccessor;
            }

            String adapterCode = "new " + TypeUtils.className(KnownTypeAdapters.MapTypeAdapter.class) + arguments +
                    "(" + keyAdapterAccessor + ", " + valueAdapterAccessor + ", " +
                    mapInstantiator + ")";
            fieldName = TYPE_ADAPTER_FIELD_PREFIX + adapterFieldInfo.size();
            adapterFieldInfo.addField(fieldType, fieldName, adapterCode);
            return fieldName;
        } else {
            return getAdapterForUnknownGenericType(fieldType, stagGenerator, typeVarsMap, adapterFieldInfo);
        }
    }

    @NotNull
    private AdapterFieldInfo addAdapterFields(@NotNull StagGenerator stagGenerator,
                                              @NotNull MethodSpec.Builder constructorBuilder,
                                              @NotNull Map<FieldAccessor, TypeMirror> memberVariables,
                                              @NotNull Map<TypeMirror, String> typeVarsMap) {

        AdapterFieldInfo result = new AdapterFieldInfo(memberVariables.size());
        for (Map.Entry<FieldAccessor, TypeMirror> entry : memberVariables.entrySet()) {
            FieldAccessor fieldAccessor = entry.getKey();
            TypeMirror fieldType = entry.getValue();

            String adapterAccessor = null;
            TypeMirror optionalJsonAdapter = fieldAccessor.getJsonAdapterType();
            if (optionalJsonAdapter != null) {
                ExecutableElement constructor = ElementUtils.getFirstConstructor(optionalJsonAdapter);
                if (constructor != null) {
                    TypeUtils.JsonAdapterType jsonAdapterType1 = TypeUtils.getJsonAdapterType(optionalJsonAdapter);
                    String initiazationCode = getInitializationCodeForKnownJsonAdapterType(constructor, stagGenerator,
                            typeVarsMap, constructorBuilder, fieldType,
                            jsonAdapterType1, result, fieldAccessor.isJsonAdapterNullSafe(), fieldAccessor.getJsonName());

                    String fieldName = TYPE_ADAPTER_FIELD_PREFIX + result.size();
                    result.addFieldToAccessor(fieldAccessor.getJsonName(), fieldName, fieldType, initiazationCode);
                } else {
                    throw new IllegalStateException("Unsupported @JsonAdapter value: " + optionalJsonAdapter);
                }
            } else if (KnownTypeAdapterUtils.hasNativePrimitiveTypeAdapter(fieldType)) {
                adapterAccessor = KnownTypeAdapterUtils.getNativePrimitiveTypeAdapter(fieldType);
            } else if (TypeUtils.containsTypeVarParams(fieldType)) {
                adapterAccessor = getAdapterForUnknownGenericType(fieldType, stagGenerator, typeVarsMap, result);
            } else {
                adapterAccessor = getAdapterAccessor(fieldType, stagGenerator, typeVarsMap, result);
            }

            if (null != adapterAccessor) {
                result.addTypeToAdapterAccessor(fieldType, adapterAccessor);
            }
        }
        return result;
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
    public TypeSpec createTypeAdapterSpec(@NotNull StagGenerator stagGenerator) {
        TypeMirror typeMirror = mInfo.getType();
        TypeName typeVariableName = TypeVariableName.get(typeMirror);

        List<? extends TypeMirror> typeArguments = mInfo.getTypeArguments();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson");

        String className = FileGenUtils.unescapeEscapedString(mInfo.getTypeAdapterClassName());
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(className)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"unchecked\"")
                        .addMember("value", "\"rawtypes\"")
                        .build())
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .superclass(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeVariableName));

        Map<TypeMirror, String> typeVarsMap = new HashMap<>();

        int idx = 0;
        if (null != typeArguments) {
            for (TypeMirror innerTypeMirror : typeArguments) {
                if (innerTypeMirror.getKind() == TypeKind.TYPEVAR) {
                    TypeVariable typeVariable = (TypeVariable) innerTypeMirror;
                    String simpleName = typeVariable.asElement().getSimpleName().toString();
                    adapterBuilder.addTypeVariable(TypeVariableName.get(simpleName, TypeVariableName.get(typeVariable.getUpperBound())));
                    //If the classInfo has unknown types, pass type... as param in constructor.
                    String paramName = "type[" + String.valueOf(idx) + "]";
                    typeVarsMap.put(typeVariable, paramName);
                    idx++;
                }
            }
        }

        if (idx > 0) {
            constructorBuilder.addParameter(Type[].class, "type");
            constructorBuilder.varargs(true);
        } else {
            //Create Type token as a static public final member variable
            // to be used from outside and by other adapters
            adapterBuilder.addField(createTypeTokenSpec(typeMirror));
        }

        AnnotatedClass annotatedClass = mSupportedTypesModel.getSupportedType(typeMirror);
        if (null == annotatedClass) {
            throw new IllegalStateException("The AnnotatedClass class can't be null in TypeAdapterGenerator : " + typeMirror.toString());
        }
        Map<FieldAccessor, TypeMirror> memberVariables = annotatedClass.getMemberVariables();

        AdapterFieldInfo adapterFieldInfo =
                addAdapterFields(stagGenerator, constructorBuilder, memberVariables, typeVarsMap);


        MethodSpec writeMethod = getWriteMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);
        MethodSpec readMethod = getReadMethodSpec(typeVariableName, memberVariables, adapterFieldInfo);

        adapterBuilder.addField(Gson.class, "mGson", Modifier.FINAL, Modifier.PRIVATE);
        constructorBuilder.addStatement("this.mGson = gson");


        for (Map.Entry<String, FieldInfo> fieldInfo : adapterFieldInfo.mTypeTokenAccessorFields.entrySet()) {
            String originalFieldName = FileGenUtils.unescapeEscapedString(fieldInfo.getValue().accessorVariable);
            TypeName typeName = getTypeTokenFieldTypeName(fieldInfo.getValue().type);
            constructorBuilder.addStatement(typeName.toString() + " " + originalFieldName + " = " + fieldInfo.getValue().initializationCode);
        }

        for (Map.Entry<String, FieldInfo> fieldInfo : adapterFieldInfo.mFieldAdapterAccessor.entrySet()) {
            String originalFieldName = FileGenUtils.unescapeEscapedString(fieldInfo.getValue().accessorVariable);
            TypeName typeName = getAdapterFieldTypeName(fieldInfo.getValue().type);
            adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
            constructorBuilder.addStatement("this." + originalFieldName + " = " + fieldInfo.getValue().initializationCode);
        }

        for (Map.Entry<String, FieldInfo> fieldInfo : adapterFieldInfo.mAdapterFields.entrySet()) {
            String originalFieldName = FileGenUtils.unescapeEscapedString(fieldInfo.getValue().accessorVariable);
            TypeName typeName = getAdapterFieldTypeName(fieldInfo.getValue().type);
            adapterBuilder.addField(typeName, originalFieldName, Modifier.PRIVATE, Modifier.FINAL);
            constructorBuilder.addStatement("this." + originalFieldName + " = " + fieldInfo.getValue().initializationCode);
        }

        adapterBuilder.addMethod(constructorBuilder.build());
        adapterBuilder.addMethod(writeMethod);
        adapterBuilder.addMethod(readMethod);

        return adapterBuilder.build();
    }

    private static class FieldInfo {
        @NotNull
        private final TypeMirror type;
        @NotNull
        private final String initializationCode;
        @NotNull
        private final String accessorVariable;

        FieldInfo(@NotNull TypeMirror type, @NotNull String initializationCode, @NotNull String accessorVariable) {
            this.type = type;
            this.initializationCode = initializationCode;
            this.accessorVariable = accessorVariable;
        }
    }

    private static class AdapterFieldInfo {

        //Type.toString -> Accessor Map
        @NotNull
        private final Map<String, String> mAdapterAccessor;

        //FieldName -> Accessor Map
        @NotNull
        private final Map<String, FieldInfo> mFieldAdapterAccessor;

        //Type.toString -> Accessor Map
        @NotNull
        private final Map<String, FieldInfo> mAdapterFields;

        //Type.toString -> Type Token Accessor Map
        @NotNull
        private final Map<String, FieldInfo> mTypeTokenAccessorFields;

        AdapterFieldInfo(int capacity) {
            mAdapterFields = new LinkedHashMap<>(capacity);
            mAdapterAccessor = new HashMap<>(capacity);
            mFieldAdapterAccessor = new HashMap<>(capacity);
            mTypeTokenAccessorFields = new LinkedHashMap<>();
        }

        String getAdapterAccessor(@NotNull TypeMirror typeMirror, @NotNull String fieldName) {
            FieldInfo adapterAccessor = mFieldAdapterAccessor.get(fieldName);
            return null != adapterAccessor ? adapterAccessor.accessorVariable : mAdapterAccessor.get(typeMirror.toString());
        }

        String updateAndGetTypeTokenFieldName(@NotNull TypeMirror fieldType, @NotNull String initializationCode) {
            FieldInfo result = mTypeTokenAccessorFields.get(fieldType.toString());
            if (null == result) {
                result = new FieldInfo(fieldType, initializationCode, "typeToken" + mTypeTokenAccessorFields.size());
                mTypeTokenAccessorFields.put(fieldType.toString(), result);
            }
            return result.accessorVariable;
        }

        String getFieldName(@NotNull TypeMirror fieldType) {
            FieldInfo fieldInfo = mAdapterFields.get(fieldType.toString());
            return null != fieldInfo ? fieldInfo.accessorVariable : null;
        }

        int size() {
            return mAdapterFields.size() + mFieldAdapterAccessor.size();
        }

        void addField(@NotNull TypeMirror fieldType, @NotNull String fieldName, @NotNull String fieldInitializationCode) {
            mAdapterFields.put(fieldType.toString(), new FieldInfo(fieldType, fieldInitializationCode, fieldName));
        }

        void addTypeToAdapterAccessor(@NotNull TypeMirror typeMirror, String accessorCode) {
            mAdapterAccessor.put(typeMirror.toString(), accessorCode);
        }

        void addFieldToAccessor(@NotNull String fieldName, @NotNull String variableName, TypeMirror fieldType, @NotNull String fieldInitializationCode) {
            mFieldAdapterAccessor.put(fieldName, new FieldInfo(fieldType, fieldInitializationCode, variableName));
        }
    }
}
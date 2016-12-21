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
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
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
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class StagGenerator {

    private static final String CLASS_STAG = "Stag";
    private static final String CLASS_TYPE_ADAPTER_FACTORY = "Factory";
    //Type.toString() -> NumberOf
    private final static HashMap<String, GenericClassInfo> KNOWN_MAP_GENERIC_CLASSES = new HashMap<>();
    private final static HashMap<String, GenericClassInfo> KNOWN_COLLECTION_GENERIC_CLASSES = new HashMap<>();

    static {
        KNOWN_MAP_GENERIC_CLASSES.put(Map.class.getName(), new GenericClassInfo(2, false));
        KNOWN_MAP_GENERIC_CLASSES.put(HashMap.class.getName(), new GenericClassInfo(2, false));
        KNOWN_MAP_GENERIC_CLASSES.put(LinkedHashMap.class.getName(), new GenericClassInfo(2, false));
        KNOWN_MAP_GENERIC_CLASSES.put(ConcurrentHashMap.class.getName(), new GenericClassInfo(2, false));

        KNOWN_COLLECTION_GENERIC_CLASSES.put(Collection.class.getName(), new GenericClassInfo(1, false));
        KNOWN_COLLECTION_GENERIC_CLASSES.put(List.class.getName(), new GenericClassInfo(1, false));
        KNOWN_COLLECTION_GENERIC_CLASSES.put(ArrayList.class.getName(), new GenericClassInfo(1, false));
    }

    @NotNull
    private final Filer mFiler;
    @NotNull
    private final List<ClassInfo> mKnownClasses;
    @NotNull
    private final HashMap<String, String> mFieldNameMap = new HashMap<>();
    @NotNull
    private final HashMap<String, String> mUnknownAdapterFieldMap = new HashMap<>();
    @NotNull
    private final HashMap<TypeMirror, String> mConcreteAdapterFieldMap = new HashMap<>();
    @NotNull
    private final List<ClassInfo> mUnknownClasses = new ArrayList<>();
    //Type.toString() -> NumberOf
    @NotNull
    private final HashMap<String, GenericClassInfo> mGenericClassInfo = new HashMap<>();
    @NotNull
    private final String mGeneratedPackageName;

    public StagGenerator(@NotNull String generatedPackageName, @NotNull Filer filer,
                         @NotNull Set<TypeMirror> knownTypes) {
        mFiler = filer;
        mGeneratedPackageName = generatedPackageName;
        mKnownClasses = new ArrayList<>(knownTypes.size());
        Set<String> knownFieldNames = new HashSet<>(knownTypes.size());
        Set<ClassInfo> genericClasses = new HashSet<>();
        for (TypeMirror knownType : knownTypes) {
            String adapterFactoryMethodName = null;
            ClassInfo classInfo = new ClassInfo(knownType);
            List<? extends TypeMirror> typeArguments = classInfo.getTypeArguments();
            if (null == typeArguments || typeArguments.isEmpty()) {
                adapterFactoryMethodName = classInfo.getTypeAdapterClassName();
                if (knownFieldNames.contains(adapterFactoryMethodName)) {
                    adapterFactoryMethodName =
                            adapterFactoryMethodName + String.valueOf(knownFieldNames.size());
                }
                knownFieldNames.add(adapterFactoryMethodName);
            } else {
                genericClasses.add(classInfo);
            }
            mKnownClasses.add(classInfo);
            mFieldNameMap.put(knownType.toString(), adapterFactoryMethodName);
        }


        for (ClassInfo knownGenericType : genericClasses) {
            List<? extends TypeMirror> typeArguments = knownGenericType.getTypeArguments();
            AnnotatedClass annotatedClass = SupportedTypesModel.getInstance().getSupportedType(knownGenericType.getType());
            Map<Element, TypeMirror> memberVariables = annotatedClass.getMemberVariables();
            boolean hasUnknownTypeFields = false;
            for (TypeMirror type : memberVariables.values()) {
                if (!checkKnownAdapters(type)) {
                    hasUnknownTypeFields = true;
                    break;
                }
            }
            mGenericClassInfo.put(knownGenericType.getType().toString(), new GenericClassInfo(typeArguments.size(), hasUnknownTypeFields));
        }

        KnownTypeAdapterUtils.initialize();
    }

    private boolean checkKnownAdapters(@NotNull TypeMirror typeMirror) {
        if (typeMirror.getKind() == TypeKind.TYPEVAR) {
            return true;
        }

        if (TypeUtils.isConcreteType(typeMirror)) {
            return true;
        }

        if (typeMirror instanceof DeclaredType) {
            DeclaredType declaredType = ((DeclaredType) typeMirror);
            Element outerClassType = declaredType.asElement();
            if (!mFieldNameMap.containsKey(outerClassType.asType().toString()) &&
                    !KNOWN_COLLECTION_GENERIC_CLASSES.containsKey(outerClassType.toString()) &&
                    !KNOWN_MAP_GENERIC_CLASSES.containsKey(outerClassType.toString())) {
                return false;
            }

            List<? extends TypeMirror> typeMirrors = ((DeclaredType) typeMirror).getTypeArguments();
            for (TypeMirror type : typeMirrors) {
                if (!checkKnownAdapters(type)) {
                    return false;
                }
            }
            return true;
        }

        return mFieldNameMap.get(typeMirror.toString()) != null;
    }

    @Nullable
    public GenericClassInfo getGenericClassInfo(@NotNull TypeMirror typeMirror) {
        return mGenericClassInfo.get(typeMirror.toString());
    }

    @Nullable
    String getClassAdapterFactoryMethod(@NotNull TypeMirror fieldType) {
        return TypeUtils.isConcreteType(fieldType) ? mFieldNameMap.get(fieldType.toString()) : null;
    }

    /**
     * Generates the public API in the form of the {@code Stag.Factory} type adapter factory
     * for the annotated classes.
     *
     * @throws IOException throws an exception
     *                     if we are unable to write the file
     *                     to the filesystem.
     */
    public void generateTypeAdapterFactory(@NotNull String generatedPackageName) throws IOException {
        TypeSpec.Builder adaptersBuilder =
                TypeSpec.classBuilder(CLASS_STAG).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        adaptersBuilder.addType(getAdapterFactorySpec());

        JavaFile javaFile = JavaFile.builder(generatedPackageName, adaptersBuilder.build()).build();
        FileGenUtils.writeToFile(javaFile, mFiler);
    }

    @NotNull
    private TypeSpec getAdapterFactorySpec() {
        TypeVariableName genericTypeName = TypeVariableName.get("T");

        TypeSpec.Builder adapterFactoryBuilder = TypeSpec.classBuilder(CLASS_TYPE_ADAPTER_FACTORY)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(TypeAdapterFactory.class);

        MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName), "type")
                .addStatement("Class<? super T> clazz = type.getRawType()");

        /*
         * Iterate through all the registered unknown classes, and map the classes to its corresponding type adapters.
         */
        for (ClassInfo classInfo : mUnknownClasses) {
            String variableName = mUnknownAdapterFieldMap.get(classInfo.getType().toString());
            TypeName typeName = TypeVariableName.get(classInfo.getType());
            TypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
            String fieldName = "mAdapter" + variableName;
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(parameterizedTypeName, FileGenUtils.unescapeEscapedString(fieldName), Modifier.PRIVATE);
            adapterFactoryBuilder.addField(fieldSpecBuilder.build());
            String getAdapterFactoryMethodName = "get" + variableName;

            MethodSpec.Builder getAdapterMethodBuilder = MethodSpec.methodBuilder(FileGenUtils.unescapeEscapedString(getAdapterFactoryMethodName))
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(Gson.class, "gson")
                    .returns(parameterizedTypeName);
            getAdapterMethodBuilder.beginControlFlow("if (null == " + fieldName + ")");

            String knownTypeAdapterForType = KnownTypeAdapterUtils.getKnownTypeAdapterForType(classInfo.getType().toString());
            if (TypeAdapterGenerator.isArray(classInfo.getType())) {
                TypeMirror typeMirror = getFirstTypeArgument(classInfo);
                String valueAdapterName = mFieldNameMap.get(typeMirror.toString());
                String listInstantiater = KnownTypeAdapterUtils.getListInstantiater(classInfo.getType());
                getAdapterMethodBuilder.addStatement(fieldName + " = new com.vimeo.stag.KnownTypeAdapters.ListTypeAdapter(get" + valueAdapterName + "(gson), new " + listInstantiater + "())");
            } else if (TypeAdapterGenerator.isMap(classInfo.getType())) {
                TypeMirror keyTypeMirror = getFirstTypeArgument(classInfo);
                TypeMirror valueTypeMirror = getSecondTypeArgument(classInfo);
                String mapInstantiater = KnownTypeAdapterUtils.getMapInstantiater(classInfo.getType());

                String result;
                String keyFieldName = KnownTypeAdapterUtils.getKnownTypeAdapterForType(keyTypeMirror.toString());
                String valueFieldName = KnownTypeAdapterUtils.getKnownTypeAdapterForType(valueTypeMirror.toString());

                if (keyFieldName == null) {
                    keyFieldName = mFieldNameMap.get(keyTypeMirror.toString());
                    if (keyFieldName == null) {
                        ClassInfo keyClassInfo = new ClassInfo(keyTypeMirror);
                        String adapterName = "mAdapter" + keyClassInfo.getTypeAdapterClassName();
                        ParameterizedTypeName keyParameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), TypeVariableName.get(keyClassInfo.getType()));
                        FieldSpec.Builder keyFieldSpecBuilder = FieldSpec.builder(keyParameterizedTypeName, FileGenUtils.unescapeEscapedString(adapterName), Modifier.PRIVATE);
                        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(FileGenUtils.unescapeEscapedString("get" + keyClassInfo.getTypeAdapterClassName()))
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(Gson.class, "gson")
                                .returns(keyParameterizedTypeName);
                        methodBuilder.beginControlFlow("if (null == " + adapterName + ")");
                        methodBuilder.addStatement(adapterName + " = gson.getAdapter(new TypeToken<" + keyClassInfo.getType().toString() + ">(){})");
                        methodBuilder.endControlFlow();
                        methodBuilder.addStatement("return " + adapterName);
                        adapterFactoryBuilder.addField(keyFieldSpecBuilder.build());
                        adapterFactoryBuilder.addMethod(methodBuilder.build());
                        keyFieldName = keyClassInfo.getTypeAdapterClassName();
                    }
                    result = " = new com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter(get" + keyFieldName + "(gson), ";
                } else {
                    result = " = new com.vimeo.stag.KnownTypeAdapters.MapTypeAdapter(" + keyFieldName + ", ";
                }

                if (valueFieldName == null) {
                    valueFieldName = mFieldNameMap.get(valueTypeMirror.toString());
                    if (valueFieldName == null) {
                        ClassInfo valueClassInfo = new ClassInfo(valueTypeMirror);
                        String adapterName = "mAdapter" + valueClassInfo.getTypeAdapterClassName();
                        ParameterizedTypeName keyParameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), TypeVariableName.get(valueClassInfo.getType()));
                        FieldSpec.Builder keyFieldSpecBuilder = FieldSpec.builder(keyParameterizedTypeName, FileGenUtils.unescapeEscapedString(adapterName), Modifier.PRIVATE);
                        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(FileGenUtils.unescapeEscapedString("get" + valueClassInfo.getTypeAdapterClassName()))
                                .addModifiers(Modifier.PUBLIC)
                                .addParameter(Gson.class, "gson")
                                .returns(keyParameterizedTypeName);
                        methodBuilder.beginControlFlow("if (null == " + adapterName + ")");
                        methodBuilder.addStatement(adapterName + " = gson.getAdapter(new TypeToken<" + valueClassInfo.getType().toString() + ">(){})");
                        methodBuilder.endControlFlow();
                        methodBuilder.addStatement("return " + adapterName);
                        adapterFactoryBuilder.addField(keyFieldSpecBuilder.build());
                        adapterFactoryBuilder.addMethod(methodBuilder.build());
                        valueFieldName = valueClassInfo.getTypeAdapterClassName();
                    }
                    result += "get" + valueFieldName + "(gson), new " + mapInstantiater + "())";
                } else {
                    result += valueFieldName + ", new " + mapInstantiater + "())";
                }

                getAdapterMethodBuilder.addStatement(fieldName + result);
            } else if (knownTypeAdapterForType != null) {

            } else {
                getAdapterMethodBuilder.addStatement(fieldName + " = gson.getAdapter(new TypeToken<" + classInfo.getType().toString() + ">(){})");
            }

            getAdapterMethodBuilder.endControlFlow();
            getAdapterMethodBuilder.addStatement("return " + fieldName);
            adapterFactoryBuilder.addMethod(getAdapterMethodBuilder.build());
        }

        /*
         * Iterate through all the registered known classes, and map the classes to its corresponding type adapters.
         */
        for (ClassInfo classInfo : mKnownClasses) {
            String qualifiedTypeAdapterName = classInfo.getTypeAdapterQualifiedClassName();
            List<? extends TypeMirror> typeArguments = classInfo.getTypeArguments();
            if (null == typeArguments || typeArguments.isEmpty()) {
                /*
                 *  This is used to generate the code if the class does not have any type arguments, or it is not parameterized.
                 */
                String variableName = mFieldNameMap.get(classInfo.getType().toString());
                TypeName typeName = TypeVariableName.get(classInfo.getType());
                TypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
                String fieldName = "mAdapter" + variableName;
                FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(parameterizedTypeName, FileGenUtils.unescapeEscapedString(fieldName), Modifier.PRIVATE);
                adapterFactoryBuilder.addField(fieldSpecBuilder.build());
                String getAdapterFactoryMethodName = "get" + variableName;
                //Build a getter method
                MethodSpec.Builder getAdapterMethodBuilder = MethodSpec.methodBuilder(
                        FileGenUtils.unescapeEscapedString(getAdapterFactoryMethodName))
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Gson.class, "gson")
                        .returns(parameterizedTypeName);

                getAdapterMethodBuilder.beginControlFlow("if (null == " + fieldName + ")");
                getAdapterMethodBuilder.addStatement(
                        fieldName + " = new " + qualifiedTypeAdapterName + "(gson, this)");
                getAdapterMethodBuilder.endControlFlow();
                getAdapterMethodBuilder.addStatement("return " + fieldName);
                adapterFactoryBuilder.addMethod(getAdapterMethodBuilder.build());

                createMethodBuilder.beginControlFlow(
                        "if (clazz == " + classInfo.getClassAndPackage() + ".class)");
                createMethodBuilder.addStatement(
                        "return (TypeAdapter<T>) " + getAdapterFactoryMethodName + "(gson)");
                createMethodBuilder.endControlFlow();
                createMethodBuilder.addCode("\n");
            } else {
                /*
                 *  This is used to generate the code if the class has type arguments, or it is parameterized.
                 */
                createMethodBuilder.beginControlFlow(
                        "if (clazz == " + classInfo.getClassAndPackage() + ".class)");
                createMethodBuilder.addStatement("java.lang.reflect.Type parameters = type.getType()");
                createMethodBuilder.beginControlFlow(
                        "if (parameters instanceof java.lang.reflect.ParameterizedType)");
                createMethodBuilder.addStatement(
                        "java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters");
                createMethodBuilder.addStatement(
                        "java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments()");
                String statement = "return (TypeAdapter<T>) new " + qualifiedTypeAdapterName + "(gson, this";
                for (int idx = 0; idx < typeArguments.size(); idx++) {
                    createMethodBuilder.addStatement("TypeAdapter typeAdapter" + idx + " = gson.getAdapter(TypeToken.get(parametersType[" + idx + "]))");
                    statement += ", typeAdapter" + idx;
                }
                statement += ")";
                createMethodBuilder.addStatement(statement);
                createMethodBuilder.endControlFlow();
                createMethodBuilder.beginControlFlow("else");
                createMethodBuilder.addStatement("TypeToken objectToken = TypeToken.get(Object.class)");
                createMethodBuilder.addStatement("TypeAdapter typeAdapter = gson.getAdapter(objectToken)");
                statement = "return (TypeAdapter<T>) new " + qualifiedTypeAdapterName + "(gson, this";
                for (int idx = 0; idx < typeArguments.size(); idx++) {
                    statement += ", typeAdapter";
                }
                statement += ")";
                createMethodBuilder.addStatement(statement);
                createMethodBuilder.endControlFlow();
                createMethodBuilder.endControlFlow();
                createMethodBuilder.addCode("\n");
            }
        }

        Set<Map.Entry<TypeMirror, String>> entries = mConcreteAdapterFieldMap.entrySet();
        for (Map.Entry<TypeMirror, String> entry : entries) {
            TypeMirror fieldType = entry.getKey();

            String adapterCode = entry.getValue();
            TypeName typeName = TypeVariableName.get(fieldType);
            TypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
            String variableName = "m" + generateNameFromType(fieldType);
            variableName = variableName.replaceAll("\\[", "").replaceAll("\\]", "");
            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(parameterizedTypeName, variableName, Modifier.PRIVATE);
            MethodSpec.Builder getAdapterMethodBuilder = MethodSpec.methodBuilder("get" + generateNameFromType(fieldType))
                    .addModifiers(Modifier.PUBLIC)
                    .returns(parameterizedTypeName);
            getAdapterMethodBuilder.beginControlFlow("if (" + variableName + " == null)");
            getAdapterMethodBuilder.addStatement(variableName + " = " + adapterCode);
            getAdapterMethodBuilder.endControlFlow();
            getAdapterMethodBuilder.addStatement("return " + variableName);
            adapterFactoryBuilder.addField(fieldSpecBuilder.build());
            adapterFactoryBuilder.addMethod(getAdapterMethodBuilder.build());
        }

        createMethodBuilder.addStatement("return null");
        adapterFactoryBuilder.addMethod(createMethodBuilder.build());

        return adapterFactoryBuilder.build();
    }

    /**
     * Returns the {@link TypeVariableName} of Stag.Factory file. This is used to get the type adapters
     * that are already generated in the stag file, avoiding recreating the same type adapters.
     *
     * @return {@link TypeVariableName}
     */
    @NotNull
    TypeVariableName getGeneratedClassName() {
        return TypeVariableName.get(
                mGeneratedPackageName + "." + CLASS_STAG + "." + CLASS_TYPE_ADAPTER_FACTORY);
    }

    @NotNull
    String addFieldType(@NotNull TypeMirror fieldType) {
        String fieldTypeString = fieldType.toString();
        String result = mUnknownAdapterFieldMap.get(fieldTypeString);
        if (null == result) {
            ClassInfo classInfo = new ClassInfo(fieldType);
            StringBuilder fieldNameBuilder = new StringBuilder();
            boolean makeCapital = true;
            for (int idx = 0; idx < fieldTypeString.length(); idx++) {
                char c = fieldTypeString.charAt(idx);
                if (c == '.' || c == '<' || c == ',' || c == '>') {
                    makeCapital = true;
                } else {
                    fieldNameBuilder.append(makeCapital ? Character.toUpperCase(c) : c);
                    makeCapital = false;
                }
            }
            result = fieldNameBuilder.toString();
            mUnknownClasses.add(classInfo);
            mUnknownAdapterFieldMap.put(fieldTypeString, result);
        }
        return result;
    }

    @NotNull
    String addConcreteFieldType(@NotNull TypeMirror fieldType, @NotNull String adapterAccessorCode) {
        String result = mConcreteAdapterFieldMap.get(fieldType);
        if (result == null) {
            if (mUnknownAdapterFieldMap.get(fieldType.toString()) == null && mFieldNameMap.get(fieldType.toString()) == null) {
                mConcreteAdapterFieldMap.put(fieldType, adapterAccessorCode);
            }
        }
        return "get" + generateNameFromType(fieldType);
    }

    @Nullable
    private TypeMirror getFirstTypeArgument(ClassInfo classInfo) {
        List<? extends TypeMirror> typeArguments = classInfo.getTypeArguments();
        return typeArguments != null ? typeArguments.get(0) : null;
    }

    @Nullable
    private TypeMirror getSecondTypeArgument(ClassInfo classInfo) {
        List<? extends TypeMirror> typeArguments = classInfo.getTypeArguments();
        return typeArguments != null ? typeArguments.get(1) : null;
    }

    @NotNull
    private String generateNameFromType(@NotNull TypeMirror typeMirror) {
        String fieldTypeString = typeMirror.toString();
        fieldTypeString = fieldTypeString.replaceAll("\\[", "").replaceAll("\\]", "");
        StringBuilder fieldNameBuilder = new StringBuilder();
        boolean makeCapital = true;
        for (int idx = 0; idx < fieldTypeString.length(); idx++) {
            char c = fieldTypeString.charAt(idx);
            if (c == '.' || c == '<' || c == ',' || c == '>') {
                makeCapital = true;
            } else {
                fieldNameBuilder.append(makeCapital ? Character.toUpperCase(c) : c);
                makeCapital = false;
            }
        }
        return fieldNameBuilder.toString();
    }

    static class GenericClassInfo {
        int mNumArguments;
        boolean mHasUnknownTypeFields;

        GenericClassInfo(int numArguments, boolean hasUnknownTypeFields) {
            mNumArguments = numArguments;
            mHasUnknownTypeFields = hasUnknownTypeFields;
        }
    }
}
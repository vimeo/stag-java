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
import com.squareup.javapoet.AnnotationSpec;
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
    @NotNull
    private final static HashMap<String, GenericClassInfo> KNOWN_MAP_GENERIC_CLASSES = new HashMap<>();
    @NotNull
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
    private final List<ClassInfo> mUnknownClasses = new ArrayList<>();
    @NotNull
    private final HashMap<String, GenericClassInfo> mGenericClassInfo = new HashMap<>();
    @NotNull
    private final String mGeneratedPackageName;
    @NotNull
    private final HashMap<String, String> mConcreteAdapterFieldMap = new HashMap<>();
    @NotNull
    private final HashMap<String, String> mGenericAdapterFieldMap = new HashMap<>();
    @NotNull
    private final Set<TypeMirror> mKnownTypes;

    public StagGenerator(@NotNull String generatedPackageName, @NotNull Filer filer, @NotNull Set<TypeMirror> knownTypes) {
        mFiler = filer;
        mKnownTypes = knownTypes;
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
    }

    Set<TypeMirror> getKnownTypes() {
        return mKnownTypes;
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
    GenericClassInfo getGenericClassInfo(@NotNull TypeMirror typeMirror) {
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
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "\"unchecked\"").build())
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName), "type")
                .addStatement("Class<? super T> clazz = type.getRawType()");

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

                GenericClassInfo genericClassInfo = mGenericClassInfo.get(classInfo.getType().toString());
                boolean hasUnknownTypes = null != genericClassInfo && genericClassInfo.mHasUnknownVarTypeFields;

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
                    if (!hasUnknownTypes) {
                        createMethodBuilder.addStatement("TypeAdapter typeAdapter" + idx + " = gson.getAdapter(TypeToken.get(parametersType[" + idx + "]))");
                        statement += ", typeAdapter" + idx;
                    } else {
                        statement += ", parametersType[" + idx + "]";
                    }
                }

                statement += ")";
                createMethodBuilder.addStatement(statement);
                createMethodBuilder.endControlFlow();
                createMethodBuilder.beginControlFlow("else");
                createMethodBuilder.addStatement("TypeToken objectToken = TypeToken.get(Object.class)");
                statement = "return (TypeAdapter<T>) new " + qualifiedTypeAdapterName + "(gson, this";
                if (!hasUnknownTypes) {
                    createMethodBuilder.addStatement("TypeAdapter typeAdapter = gson.getAdapter(objectToken)");
                }
                for (int idx = 0; idx < typeArguments.size(); idx++) {
                    if (!hasUnknownTypes) {
                        statement += ", typeAdapter";
                    } else {
                        statement += ", objectToken.getType()";
                    }

                }
                statement += ")";
                createMethodBuilder.addStatement(statement);
                createMethodBuilder.endControlFlow();
                createMethodBuilder.endControlFlow();
                createMethodBuilder.addCode("\n");
            }
        }

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
            if (null != knownTypeAdapterForType) {
                fieldName += knownTypeAdapterForType;
            } else {
                getAdapterMethodBuilder.addStatement(fieldName + " = gson.getAdapter(new TypeToken<" + classInfo.getType().toString() + ">(){})");
            }
            getAdapterMethodBuilder.endControlFlow();
            getAdapterMethodBuilder.addStatement("return " + fieldName);
            adapterFactoryBuilder.addMethod(getAdapterMethodBuilder.build());
        }

        /*
         * Iterate through all the registered concrete fields, and map the fields to its corresponding type adapters.
         */
        Set<Map.Entry<String, String>> concreteAdapterFieldEntries = mConcreteAdapterFieldMap.entrySet();
        for (Map.Entry<String, String> entry : concreteAdapterFieldEntries) {
            generateAdapterMethod(entry.getKey(), entry.getValue(), adapterFactoryBuilder, false);
        }

        /*
         * Iterate through all the registered generic fields, and map the fields to its corresponding type adapters.
         */
        Set<Map.Entry<String, String>> genericAdapterFieldEntries = mGenericAdapterFieldMap.entrySet();
        for (Map.Entry<String, String> entry : genericAdapterFieldEntries) {
            generateAdapterMethod(entry.getKey(), entry.getValue(), adapterFactoryBuilder, true);
        }

        createMethodBuilder.addStatement("return null");
        adapterFactoryBuilder.addMethod(createMethodBuilder.build());

        return adapterFactoryBuilder.build();
    }

    private void generateAdapterMethod(String fieldType, String adapterCode, TypeSpec.Builder adapterFactoryBuilder, boolean containsTypeArg) {
        TypeName typeName = TypeVariableName.get(fieldType);
        TypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), typeName);
        String getterName = generateNameFromType(fieldType, false);
        String variableName = "m" + getterName;
        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(parameterizedTypeName, variableName, Modifier.PRIVATE);
        MethodSpec.Builder getAdapterMethodBuilder = MethodSpec.methodBuilder("get" + getterName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Gson.class, "gson")
                .returns(parameterizedTypeName);
        getAdapterMethodBuilder.beginControlFlow("if (" + variableName + " == null)");

        if (containsTypeArg) {
            getAdapterMethodBuilder.addStatement(variableName + " = " + adapterCode, "$");
        } else {
            getAdapterMethodBuilder.addStatement(variableName + " = " + adapterCode);
        }

        getAdapterMethodBuilder.endControlFlow();
        getAdapterMethodBuilder.addStatement("return " + variableName);
        adapterFactoryBuilder.addField(fieldSpecBuilder.build());
        adapterFactoryBuilder.addMethod(getAdapterMethodBuilder.build());
    }

    /**
     * Returns the {@link TypeVariableName} of Stag.Factory file. This is used to get the type adapters
     * that are already generated in the stag file, avoiding recreating the same type adapters.
     *
     * @return {@link TypeVariableName}
     */
    @NotNull
    TypeVariableName getGeneratedClassName() {
        return TypeVariableName.get(mGeneratedPackageName + "." + CLASS_STAG + "." + CLASS_TYPE_ADAPTER_FACTORY);
    }

    /**
     * Used to add fields for the unknown types
     */
    @NotNull
    String addFieldForUnknownType(@NotNull TypeMirror fieldType) {
        String fieldTypeString = fieldType.toString();
        String result = mUnknownAdapterFieldMap.get(fieldTypeString);
        if (null == result) {
            ClassInfo classInfo = new ClassInfo(fieldType);
            result = generateNameFromType(fieldTypeString, false);
            mUnknownClasses.add(classInfo);
            mUnknownAdapterFieldMap.put(fieldTypeString, result);
        }
        return result;
    }

    /**
     * Used to add fields for the concrete types such as for {@link Map} or {@link List} or any other
     * concrete class
     */
    @NotNull
    String addFieldForConcreteType(@NotNull TypeMirror fieldType, @NotNull String adapterAccessorCode) {
        if (!mConcreteAdapterFieldMap.containsKey(fieldType.toString())) {
            mConcreteAdapterFieldMap.put(fieldType.toString(), adapterAccessorCode);
        }
        return "get" + generateNameFromType(fieldType.toString(), true);
    }

    /**
     * Used to add fields for the generic types eg: generic classes
     */
    @NotNull
    String addFieldForGenericType(@NotNull TypeMirror fieldType, @NotNull String adapterAccessorCode) {
        if (!mGenericAdapterFieldMap.containsKey(fieldType.toString())) {
            mGenericAdapterFieldMap.put(fieldType.toString(), adapterAccessorCode);
        }
        return "get" + generateNameFromType(fieldType.toString(), false);
    }

    @NotNull
    private String generateNameFromType(@NotNull String name, boolean isArray) {
        name = name.replaceAll("\\[", "").replaceAll("\\]", "");
        StringBuilder fieldNameBuilder = new StringBuilder();
        boolean makeCapital = true;
        for (int idx = 0; idx < name.length(); idx++) {
            char c = name.charAt(idx);
            if (c == '.' || c == '<' || c == ',' || c == '>') {
                makeCapital = true;
            } else {
                fieldNameBuilder.append(makeCapital ? Character.toUpperCase(c) : c);
                makeCapital = false;
            }
        }

        return isArray ? fieldNameBuilder.toString() + "Array" : fieldNameBuilder.toString();
    }

    static class GenericClassInfo {
        int mNumArguments;
        boolean mHasUnknownVarTypeFields;

        GenericClassInfo(int numArguments, boolean hasUnknownVarTypeFields) {
            mNumArguments = numArguments;
            mHasUnknownVarTypeFields = hasUnknownVarTypeFields;
        }
    }
}
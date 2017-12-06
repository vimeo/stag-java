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
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class StagGenerator {

    @NotNull
    private static final String CLASS_STAG = "Stag";
    @NotNull
    private static final String CLASS_TYPE_ADAPTER_FACTORY = "Factory";

    @NotNull
    private final Map<String, ClassInfo> mKnownClasses;
    @NotNull
    private List<SubFactoriesInfo> generatedStagFactoryWrappers = new ArrayList<>();

    public StagGenerator(@NotNull Set<TypeMirror> knownTypes) {
        mKnownClasses = new HashMap<>(knownTypes.size());

        for (TypeMirror knownType : knownTypes) {
            if (!TypeUtils.isAbstract(knownType)) {
                ClassInfo classInfo = new ClassInfo(knownType);
                mKnownClasses.put(knownType.toString(), classInfo);
            }
        }
    }

    public static String getGeneratedFactoryClassAndPackage(String generatedPackageName) {
        return generatedPackageName + "." + CLASS_STAG + "." + CLASS_TYPE_ADAPTER_FACTORY;
    }

    public void setGeneratedStagFactoryWrappers(@NotNull List<SubFactoriesInfo> generatedStagFactoryWrappers) {
        this.generatedStagFactoryWrappers = generatedStagFactoryWrappers;
    }

    @Nullable
    public ClassInfo getKnownClass(@NotNull TypeMirror typeMirror) {
        return mKnownClasses.get(typeMirror.toString());
    }

    /**
     * Generates the public API in the form of the {@code Stag.Factory} type adapter factory
     * for the annotated classes. Creates the spec for the class.
     *
     * @return A non null TypeSpec for the factory class.
     */
    @NotNull
    public TypeSpec createStagSpec() {
        TypeSpec.Builder stagBuilder =
                TypeSpec.classBuilder(CLASS_STAG).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        stagBuilder.addType(getAdapterFactorySpec());

        return stagBuilder.build();
    }

    @NotNull
    private TypeSpec getAdapterFactorySpec() {
        TypeVariableName genericTypeName = TypeVariableName.get("T");

        TypeSpec.Builder adapterFactoryBuilder = TypeSpec.classBuilder(CLASS_TYPE_ADAPTER_FACTORY)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(TypeAdapterFactory.class);

        ParameterizedTypeName hashMapOfStringToInteger = ParameterizedTypeName.get(ConcurrentHashMap.class, String.class, Integer.class);
        FieldSpec.Builder packageToIndexMapField = FieldSpec.builder(hashMapOfStringToInteger,
                "packageToIndexMap", Modifier.FINAL, Modifier.PRIVATE).initializer("new " + hashMapOfStringToInteger.toString() + "( " + generatedStagFactoryWrappers.size() + ")");
        adapterFactoryBuilder.addField(packageToIndexMapField.build());

        TypeVariableName typeAdapterFactoryArray = TypeVariableName.get("TypeAdapterFactory[]");
        FieldSpec.Builder typeAdapterFactoryArrayField = FieldSpec.builder(typeAdapterFactoryArray,
                "typeAdapterFactoryArray", Modifier.FINAL, Modifier.PRIVATE).initializer("new TypeAdapterFactory[" + generatedStagFactoryWrappers.size() + "]");
        adapterFactoryBuilder.addField(typeAdapterFactoryArrayField.build());

        MethodSpec.Builder getPackageNameMethodBuilder = MethodSpec.methodBuilder("getPackageName")
                .addTypeVariable(genericTypeName)
                .returns(String.class)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addCode("String name = clazz.getName();\n" +
                        "int last = name.lastIndexOf('.');\n" +
                        "return last == -1 ? null : name.substring(0, last);\n");
        adapterFactoryBuilder.addMethod(getPackageNameMethodBuilder.build());

        MethodSpec.Builder createTypeAdapterFactoryMethodBuilder = MethodSpec.methodBuilder("createTypeAdapterFactory")
                .returns(TypeAdapterFactory.class)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(int.class, "index")
                .addStatement("TypeAdapterFactory result = null")
                .beginControlFlow("switch(index)");

        int index = 0;
        for (SubFactoriesInfo subFactoriesInfo : generatedStagFactoryWrappers) {
            createTypeAdapterFactoryMethodBuilder.addCode("case " + index + " : ");
            createTypeAdapterFactoryMethodBuilder.addStatement("\t\nresult = new " + subFactoriesInfo.classAndPackageName + "()");
            createTypeAdapterFactoryMethodBuilder.addCode("\tbreak;\n");
            index++;
        }

        createTypeAdapterFactoryMethodBuilder.endControlFlow();
        createTypeAdapterFactoryMethodBuilder.addStatement("return result");
        adapterFactoryBuilder.addMethod(createTypeAdapterFactoryMethodBuilder.build());

        MethodSpec.Builder getTypeAdapterFactoryMethodBuilder = MethodSpec.methodBuilder("getTypeAdapterFactory")
                .returns(TypeAdapterFactory.class)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(int.class, "index")
                .addCode("TypeAdapterFactory typeAdapterFactory = typeAdapterFactoryArray[index];\n" +
                        "if(null == typeAdapterFactory) {\n" +
                        "   typeAdapterFactory = createTypeAdapterFactory(index);\n" +
                        "   typeAdapterFactoryArray[index] = typeAdapterFactory;\n" +
                        "}\n" +
                        "return typeAdapterFactory;\n");
        adapterFactoryBuilder.addMethod(getTypeAdapterFactoryMethodBuilder.build());

        MethodSpec.Builder getTypeAdapterMethodBuilder = MethodSpec.methodBuilder("getTypeAdapter")
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("?")), "clazz")
                .addParameter(String.class, "currentPackageName")
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName), "type")
                .addParameter(int.class, "index")
                .addCode("String packageName = getPackageName(clazz);\n" +
                        "packageToIndexMap.put(packageName, index);\n" +
                        "if(currentPackageName.equals(packageName)) {\n" +
                        "   return getTypeAdapterFactory(index).create(gson, type);\n" +
                        "}\n" +
                        "return null;\n");
        adapterFactoryBuilder.addMethod(getTypeAdapterMethodBuilder.build());

        MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"unchecked\"")
                        .addMember("value", "\"rawtypes\"")
                        .addMember("value", "\"fallthrough\"")
                        .build())
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName),
                        "type");

        createMethodBuilder.addStatement("Class<? super T> clazz = type.getRawType()");
        createMethodBuilder.addStatement("TypeAdapter<T> result = null");
        createMethodBuilder.addStatement("String currentPackageName = getPackageName(clazz)");
        createMethodBuilder.beginControlFlow("if(null == currentPackageName)");
        createMethodBuilder.addStatement("return result");
        createMethodBuilder.endControlFlow();

        createMethodBuilder.addStatement("Integer index = packageToIndexMap.get(currentPackageName)");
        createMethodBuilder.beginControlFlow("if(null != index)");
        createMethodBuilder.addStatement("TypeAdapterFactory typeAdapterFactory = getTypeAdapterFactory(index)");
        createMethodBuilder.addStatement("return typeAdapterFactory.create(gson, type)");
        createMethodBuilder.endControlFlow();

        createMethodBuilder.beginControlFlow("switch(packageToIndexMap.size())");

        int mapIndex = 0;
        for (SubFactoriesInfo subFactoriesInfo : generatedStagFactoryWrappers) {
            createMethodBuilder.addCode("case " + mapIndex + " : ");
            createMethodBuilder.addCode("\t\t\nresult = getTypeAdapter(" + subFactoriesInfo.representativeClassInfo.getClassAndPackage() + ".class, " +
                    "currentPackageName, gson, type, " + mapIndex + ");");
            createMethodBuilder.beginControlFlow("\nif(null != result)");
            createMethodBuilder.addCode("return result;\n");
            createMethodBuilder.endControlFlow();
            mapIndex++;
        }

        createMethodBuilder.addCode("\ndefault : ");
        createMethodBuilder.addCode("\t\t\nreturn null;\n");
        createMethodBuilder.endControlFlow();

        adapterFactoryBuilder.addMethod(createMethodBuilder.build());

        return adapterFactoryBuilder.build();
    }

    public static class SubFactoriesInfo {
        public ClassInfo representativeClassInfo;
        public String classAndPackageName;

        public SubFactoriesInfo(ClassInfo classInfo, String classAndPackageName) {
            this.representativeClassInfo = classInfo;
            this.classAndPackageName = classAndPackageName;
        }
    }
}
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
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
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

        AnnotationSpec suppressions = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "\"unchecked\"")
                .addMember("value", "\"rawtypes\"")
                .build();

        TypeName staticMap = ParameterizedTypeName.get(ClassName.get(ConcurrentHashMap.class), TypeVariableName.get(String.class), TypeVariableName.get(Integer.class));
        FieldSpec.Builder packageToIndexMapFieldBuilder = FieldSpec.builder(staticMap, "sPackageToIndex", Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE);
        packageToIndexMapFieldBuilder.initializer("new ConcurrentHashMap<String, Integer>(" + generatedStagFactoryWrappers.size() + ")");
        adapterFactoryBuilder.addField(packageToIndexMapFieldBuilder.build());

        FieldSpec.Builder supportedPackages = FieldSpec.builder(ArrayTypeName.of(ClassName.get(Class.class)), "sSupportedPackages", Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE);
        supportedPackages.initializer("new Class[" + generatedStagFactoryWrappers.size() + "]");
        supportedPackages.addAnnotation(suppressions);
        adapterFactoryBuilder.addField(supportedPackages.build());

        FieldSpec.Builder subTypeFactories = FieldSpec.builder(ArrayTypeName.of(ClassName.get(TypeAdapterFactory.class)), "sSubTypeFactories", Modifier.STATIC, Modifier.FINAL, Modifier.PRIVATE);
        subTypeFactories.initializer("new TypeAdapterFactory[" + generatedStagFactoryWrappers.size() + "]");
        adapterFactoryBuilder.addField(subTypeFactories.build());

        FieldSpec.Builder idxField = FieldSpec.builder(int.class, "idx", Modifier.STATIC, Modifier.PRIVATE);
        idxField.initializer("0");
        adapterFactoryBuilder.addField(idxField.build());

        CodeBlock.Builder staticCodeBlockBuilder = CodeBlock.builder();

        MethodSpec.Builder getPackageNameBuilder = MethodSpec.methodBuilder("getPackageName")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"unchecked\"")
                        .addMember("value", "\"rawtypes\"")
                        .build())
                .addParameter(ClassName.get(Class.class), "clazz")
                .returns(ClassName.get(String.class))
                .addStatement("String name = clazz.getName()")
                .addStatement("int last = name.lastIndexOf('.')")
                .addStatement("return last == -1 ? null : name.substring(0, last)");

        adapterFactoryBuilder.addMethod(getPackageNameBuilder.build());


        MethodSpec.Builder getPositionFromPackage = MethodSpec.methodBuilder("getPositionFromPackage")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(ClassName.get(String.class), "packageName")
                .returns(ClassName.get(Integer.class))
                .addStatement("Integer result = sPackageToIndex.get(packageName)")
                .beginControlFlow("if (null == result)")
                .beginControlFlow("synchronized (Stag.Factory.class)")
                .beginControlFlow("while (idx < sSupportedPackages.length)")
                .addCode("int currentIndex = idx;\n" +
                        "String pkgName = getPackageName(sSupportedPackages[currentIndex]);\n" +
                        "sPackageToIndex.put(pkgName, currentIndex);\n" +
                        "idx++;\n" +
                        "if (pkgName.equals(packageName)) {\n" +
                        "   return currentIndex;\n" +
                        "}")
                .endControlFlow()
                .endControlFlow()
                .endControlFlow()
                .addStatement("return result");

        adapterFactoryBuilder.addMethod(getPositionFromPackage.build());


        MethodSpec.Builder getFactoryMethodBuilder = MethodSpec.methodBuilder("createFactory")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .returns(ClassName.get(TypeAdapterFactory.class))
                .addParameter(ClassName.get(Integer.class),
                        "position");
        getFactoryMethodBuilder.beginControlFlow("switch(position)");

        int count = 0;
        for (SubFactoriesInfo subFactoriesInfo : generatedStagFactoryWrappers) {
            //staticCodeBlockBuilder.addStatement("sPackageToIndex.put(getPackageName(" + subFactoriesInfo.representativeClassInfo.getClassAndPackage() + ".class), " + count +")");
            staticCodeBlockBuilder.addStatement("sSupportedPackages[" + count + "] = " + subFactoriesInfo.representativeClassInfo.getClassAndPackage() + ".class");

            getFactoryMethodBuilder.addCode("case " + count + ":\n");
            getFactoryMethodBuilder.addStatement("  return new " + subFactoriesInfo.classAndPackageName + "()");
            count++;
        }
        adapterFactoryBuilder.addStaticBlock(staticCodeBlockBuilder.build());

        getFactoryMethodBuilder.endControlFlow();
        getFactoryMethodBuilder.addStatement("return null");
        adapterFactoryBuilder.addMethod(getFactoryMethodBuilder.build());


        MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                        .addMember("value", "\"unchecked\"")
                        .addMember("value", "\"rawtypes\"")
                        .build())
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName),
                        "type")
                .addStatement("Class<? super T> clazz = type.getRawType()");

        createMethodBuilder.addStatement("TypeAdapter<T> result = null");
        createMethodBuilder.addStatement("String packageName = getPackageName(clazz)");
        createMethodBuilder.beginControlFlow("if (null != packageName)");
        createMethodBuilder.addStatement("Integer position = getPositionFromPackage(packageName)");
        createMethodBuilder.beginControlFlow("if(null != position)");
        createMethodBuilder.addStatement("TypeAdapterFactory typeAdapterFactory = sSubTypeFactories[position]");
        createMethodBuilder.beginControlFlow("if(null == typeAdapterFactory)");
        createMethodBuilder.addStatement("typeAdapterFactory = createFactory(position)");
        createMethodBuilder.addStatement("sSubTypeFactories[position] = typeAdapterFactory");
        createMethodBuilder.endControlFlow();
        createMethodBuilder.addStatement("result = typeAdapterFactory.create(gson, type)");
        createMethodBuilder.endControlFlow();
        createMethodBuilder.endControlFlow();
        createMethodBuilder.addStatement("return result");


        adapterFactoryBuilder.addMethod(createMethodBuilder.build());

        return adapterFactoryBuilder.build();
    }

    public static class SubFactoriesInfo {
        public ClassInfo representativeClassInfo;
        public String classAndPackageName;

        public SubFactoriesInfo(ClassInfo classInfo, String classAndPackageName) {
            representativeClassInfo = classInfo;
            this.classAndPackageName = classAndPackageName;
        }
    }
}
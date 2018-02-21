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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class StagGenerator {

    @NotNull
    private static final String CLASS_STAG = "Stag";
    @NotNull
    private static final String CLASS_TYPE_ADAPTER_FACTORY = "Factory";

    @NotNull
    private final Map<String, ClassInfo> mKnownClasses;

    public StagGenerator(@NotNull Set<TypeMirror> knownTypes) {
        mKnownClasses = new HashMap<>(knownTypes.size());

        for (TypeMirror knownType : knownTypes) {
            if (!TypeUtils.isAbstract(knownType)) {
                ClassInfo classInfo = new ClassInfo(knownType);
                mKnownClasses.put(knownType.toString(), classInfo);
            }
        }
    }

    @Nullable
    ClassInfo getKnownClass(@NotNull TypeMirror typeMirror) {
        return mKnownClasses.get(typeMirror.toString());
    }

    /**
     * Generates the public API in the form of the {@code Stag.Factory} type adapter factory
     * for the annotated classes. Creates the spec for the class.
     *
     * @param generatedStagFactoryWrappers List of Sub Factories that have been created
     * @return A non null TypeSpec for the factory class.
     */
    @NotNull
    public static TypeSpec createStagSpec(List<SubFactoriesInfo> generatedStagFactoryWrappers) {
        TypeSpec.Builder stagBuilder =
                TypeSpec.classBuilder(CLASS_STAG).addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        stagBuilder.addType(getAdapterFactorySpec(generatedStagFactoryWrappers));

        return stagBuilder.build();
    }

    @NotNull
    private static TypeSpec getAdapterFactorySpec(@NotNull List<SubFactoriesInfo> generatedStagFactoryWrappers) {
        TypeVariableName genericTypeName = TypeVariableName.get("T");

        TypeSpec.Builder adapterFactoryBuilder = TypeSpec.classBuilder(CLASS_TYPE_ADAPTER_FACTORY)
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addSuperinterface(TypeAdapterFactory.class);

        ParameterizedTypeName hashMapOfStringToInteger = ParameterizedTypeName.get(HashMap.class, String.class, Integer.class);
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
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), genericTypeName), "clazz")
                .addStatement("String name = clazz.getName()")
                .addStatement("int last = name.lastIndexOf('.')")
                .addStatement("return last == -1 ? null : name.substring(0, last)");
        adapterFactoryBuilder.addMethod(getPackageNameMethodBuilder.build());

        MethodSpec.Builder createTypeAdapterFactoryMethodBuilder = MethodSpec.methodBuilder("createTypeAdapterFactory")
                .returns(TypeAdapterFactory.class)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addParameter(int.class, "index")
                .addStatement("TypeAdapterFactory result = null")
                .beginControlFlow("switch(index)");

        for (int index = 0; index < generatedStagFactoryWrappers.size(); index++) {
            createTypeAdapterFactoryMethodBuilder
                    .addStatement("case $L : ", index)
                    .addStatement("result = new $L()", generatedStagFactoryWrappers.get(index).classAndPackageName)
                    .addStatement("break");
        }

        createTypeAdapterFactoryMethodBuilder
                .endControlFlow()
                .addStatement("return result");
        adapterFactoryBuilder.addMethod(createTypeAdapterFactoryMethodBuilder.build());

        MethodSpec.Builder getTypeAdapterFactoryMethodBuilder = MethodSpec.methodBuilder("getTypeAdapterFactory")
                .returns(TypeAdapterFactory.class)
                .addModifiers(Modifier.PRIVATE)
                .addParameter(int.class, "index")
                .addStatement("TypeAdapterFactory typeAdapterFactory = typeAdapterFactoryArray[index]")
                .beginControlFlow("if (typeAdapterFactory == null)")
                .addStatement("typeAdapterFactory = createTypeAdapterFactory(index)")
                .addStatement("typeAdapterFactoryArray[index] = typeAdapterFactory")
                .endControlFlow()
                .addStatement("return typeAdapterFactory");
        adapterFactoryBuilder.addMethod(getTypeAdapterFactoryMethodBuilder.build());

        MethodSpec.Builder getTypeAdapterMethodBuilder = MethodSpec.methodBuilder("getTypeAdapterFactory")
                .returns(ClassName.get(TypeAdapterFactory.class))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("?")), "clazz")
                .addParameter(String.class, "currentPackageName")
                .addParameter(int.class, "index")
                .addStatement("String packageName = getPackageName(clazz)")
                .addStatement("packageToIndexMap.put(packageName, index)")
                .beginControlFlow("if (currentPackageName.equals(packageName))")
                .addStatement("return getTypeAdapterFactory(index)")
                .endControlFlow()
                .addStatement("return null");
        adapterFactoryBuilder.addMethod(getTypeAdapterMethodBuilder.build());

        MethodSpec.Builder getSubTypeAdapterMethodBuilder = MethodSpec.methodBuilder("getSubFactory")
                .returns(ClassName.get(TypeAdapterFactory.class))
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                       .addMember("value", "\"fallthrough\"")
                                       .build())
                .addModifiers(Modifier.PRIVATE, Modifier.SYNCHRONIZED)
                .addParameter(String.class, "currentPackageName")
                .addStatement("Integer index = packageToIndexMap.get(currentPackageName);")
                .beginControlFlow("if (index != null)")
                .addStatement("TypeAdapterFactory typeAdapterFactory = getTypeAdapterFactory(index)")
                .addStatement("return typeAdapterFactory")
                .endControlFlow()
                .addStatement("TypeAdapterFactory result = null")
                .beginControlFlow("switch(packageToIndexMap.size())");

        for (int index = 0; index < generatedStagFactoryWrappers.size(); index++) {
            getSubTypeAdapterMethodBuilder
                    .addStatement("case $L :", index)
                    .addStatement("result = getTypeAdapterFactory($L.class, currentPackageName, $L)", generatedStagFactoryWrappers.get(index).representativeClassInfo.getClassAndPackage(), index)
                    .beginControlFlow("if (result != null)")
                    .addStatement("return result")
                    .endControlFlow();
        }

        getSubTypeAdapterMethodBuilder
                .addStatement("default :")
                .addStatement("return null")
                .endControlFlow();

        adapterFactoryBuilder.addMethod(getSubTypeAdapterMethodBuilder.build());

        String suppressWarningValue = "value";
        MethodSpec.Builder createMethodBuilder = MethodSpec.methodBuilder("create")
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(Override.class)
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                       .addMember(suppressWarningValue, "\"unchecked\"")
                                       .addMember(suppressWarningValue, "\"rawtypes\"")
                                       .build())
                .addTypeVariable(genericTypeName)
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericTypeName))
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericTypeName),
                              "type")

                .addStatement("Class<? super T> clazz = type.getRawType()")
                .addStatement("String currentPackageName = getPackageName(clazz)")
                .beginControlFlow("if (currentPackageName == null)")
                .addStatement("return null")
                .endControlFlow()
                .addStatement("TypeAdapterFactory typeAdapterFactory = getSubFactory(currentPackageName)")
                .addStatement("return typeAdapterFactory != null ? typeAdapterFactory.create(gson, type) : null");

        adapterFactoryBuilder.addMethod(createMethodBuilder.build());

        return adapterFactoryBuilder.build();
    }

    public static class SubFactoriesInfo {

        @NotNull final ClassInfo representativeClassInfo;

        @NotNull final String classAndPackageName;

        public SubFactoriesInfo(@NotNull ClassInfo classInfo, @NotNull String classAndPackageName) {
            this.representativeClassInfo = classInfo;
            this.classAndPackageName = classAndPackageName;
        }
    }
}

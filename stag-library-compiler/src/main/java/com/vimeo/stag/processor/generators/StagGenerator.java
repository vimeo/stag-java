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
                .addCode("String name = clazz.getName();\n" +
                         "int last = name.lastIndexOf('.');\n" +
                         "return last == -1 ? null : name.substring(0, last);\n");
        adapterFactoryBuilder.addMethod(getPackageNameMethodBuilder.build());

        MethodSpec.Builder createTypeAdapterFactoryMethodBuilder = MethodSpec.methodBuilder("createTypeAdapterFactory")
                .returns(TypeAdapterFactory.class)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
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
                         "if(typeAdapterFactory == null) {\n" +
                         "   typeAdapterFactory = createTypeAdapterFactory(index);\n" +
                         "   typeAdapterFactoryArray[index] = typeAdapterFactory;\n" +
                         "}\n" +
                         "return typeAdapterFactory;\n");
        adapterFactoryBuilder.addMethod(getTypeAdapterFactoryMethodBuilder.build());

        MethodSpec.Builder getTypeAdapterMethodBuilder = MethodSpec.methodBuilder("getTypeAdapterFactory")
                .returns(ClassName.get(TypeAdapterFactory.class))
                .addModifiers(Modifier.PRIVATE)
                .addParameter(ParameterizedTypeName.get(ClassName.get(Class.class), TypeVariableName.get("?")), "clazz")
                .addParameter(String.class, "currentPackageName")
                .addParameter(int.class, "index")
                .addCode("String packageName = getPackageName(clazz);\n" +
                         "packageToIndexMap.put(packageName, index);\n" +
                         "if(currentPackageName.equals(packageName)) {\n" +
                         "   return getTypeAdapterFactory(index);\n" +
                         "}\n" +
                         "return null;\n");
        adapterFactoryBuilder.addMethod(getTypeAdapterMethodBuilder.build());

        MethodSpec.Builder getSubTypeAdapterMethodBuilder = MethodSpec.methodBuilder("getSubFactory")
                .returns(ClassName.get(TypeAdapterFactory.class))
                .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class)
                                       .addMember("value", "\"fallthrough\"")
                                       .build())
                .addModifiers(Modifier.PRIVATE, Modifier.SYNCHRONIZED)
                .addParameter(String.class, "currentPackageName");

        getSubTypeAdapterMethodBuilder.addStatement("Integer index = packageToIndexMap.get(currentPackageName);");
        getSubTypeAdapterMethodBuilder.beginControlFlow("if(index != null)");
        getSubTypeAdapterMethodBuilder.addStatement("TypeAdapterFactory typeAdapterFactory = getTypeAdapterFactory(index)");
        getSubTypeAdapterMethodBuilder.addStatement("return typeAdapterFactory");
        getSubTypeAdapterMethodBuilder.endControlFlow();
        getSubTypeAdapterMethodBuilder.addStatement("TypeAdapterFactory result = null");
        getSubTypeAdapterMethodBuilder.beginControlFlow("switch(packageToIndexMap.size())");

        int mapIndex = 0;
        for (SubFactoriesInfo subFactoriesInfo : generatedStagFactoryWrappers) {
            getSubTypeAdapterMethodBuilder.addCode("case " + mapIndex + " : ");
            getSubTypeAdapterMethodBuilder.addCode("\n");
            getSubTypeAdapterMethodBuilder.addCode("\tresult = getTypeAdapterFactory(" + subFactoriesInfo.representativeClassInfo.getClassAndPackage() + ".class, " +
                                                   "currentPackageName, " + mapIndex + ");");
            getSubTypeAdapterMethodBuilder.addCode("\n");
            getSubTypeAdapterMethodBuilder.addCode("\tif(result != null) {");
            getSubTypeAdapterMethodBuilder.addCode("\n");
            getSubTypeAdapterMethodBuilder.addStatement("\t\treturn result");
            getSubTypeAdapterMethodBuilder.addCode("\t}\n");
            mapIndex++;
        }

        getSubTypeAdapterMethodBuilder.addCode("default : ");
        getSubTypeAdapterMethodBuilder.addStatement("\t\nreturn null");
        getSubTypeAdapterMethodBuilder.endControlFlow();

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
                              "type");

        createMethodBuilder.addStatement("Class<? super T> clazz = type.getRawType()");
        createMethodBuilder.addStatement("String currentPackageName = getPackageName(clazz)");
        createMethodBuilder.beginControlFlow("if(currentPackageName == null)");
        createMethodBuilder.addStatement("return null");
        createMethodBuilder.endControlFlow();
        createMethodBuilder.addCode("\n");
        createMethodBuilder.addStatement("TypeAdapterFactory typeAdapterFactory = getSubFactory(currentPackageName)");
        createMethodBuilder.addStatement("return typeAdapterFactory != null ? typeAdapterFactory.create(gson, type) : null");

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

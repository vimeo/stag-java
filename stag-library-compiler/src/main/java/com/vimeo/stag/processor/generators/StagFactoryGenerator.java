package com.vimeo.stag.processor.generators;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.generators.model.ClassInfo;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.lang.model.element.Modifier;

public class StagFactoryGenerator {
    public static final String NAME = "StagFactory";
    private final List<ClassInfo> classInfoList;
    private final String fileName;

    public StagFactoryGenerator(List<ClassInfo> classInfoList, String fileName) {
        this.classInfoList = classInfoList;
        this.fileName = fileName;
    }

    public TypeSpec getTypeAdapterFactorySpec() {
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(fileName)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(TypeAdapterFactory.class)
                .addMethod(getCreateMethodSpec());

        return adapterBuilder.build();
    }

    @NotNull
    private MethodSpec getCreateMethodSpec() {
        TypeVariableName genericType = TypeVariableName.get("T");
        AnnotationSpec suppressions = AnnotationSpec.builder(SuppressWarnings.class)
                .addMember("value", "\"unchecked\" /* Protected by TypeToken */")
                .addMember("value", "\"rawtypes\"")
                .build();
        MethodSpec.Builder builder = MethodSpec.methodBuilder("create")
                .addTypeVariable(genericType)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericType), "type")
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericType))
                .addAnnotation(suppressions)
                .addModifiers(Modifier.PUBLIC)
                .addCode("Class<? super T> clazz = type.getRawType();\n");

        for (ClassInfo classInfo : classInfoList) {
            builder.addCode("if (clazz == " + classInfo.getClassAndPackage() + ".class) {\n" +
                    "\treturn (TypeAdapter<T>) new " + classInfo.getTypeAdapterQualifiedClassName() +
                    "(gson);\n" +
                    "}\n");
        }

        builder.addCode("return null;\n");

        return builder.build();
    }
}
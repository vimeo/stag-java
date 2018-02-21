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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class StagFactoryGenerator {

    public static final String NAME = "StagFactory";
    private final List<ClassInfo> classInfoList;
    private final String fileName;

    public StagFactoryGenerator(@NotNull List<ClassInfo> classInfoList, @NotNull String fileName) {
        this.classInfoList = new ArrayList<>(classInfoList);
        this.fileName = fileName;
    }

    @NotNull
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
                .addMember("value", "\"unchecked\"")
                .addMember("value", "\"rawtypes\"")
                .build();
        MethodSpec.Builder builder = MethodSpec.methodBuilder("create")
                .addTypeVariable(genericType)
                .addParameter(Gson.class, "gson")
                .addParameter(ParameterizedTypeName.get(ClassName.get(TypeToken.class), genericType), "type")
                .returns(ParameterizedTypeName.get(ClassName.get(TypeAdapter.class), genericType))
                .addAnnotation(suppressions)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addCode("Class<? super T> clazz = type.getRawType();\n");

        for (ClassInfo classInfo : classInfoList) {
            builder.beginControlFlow("if (clazz == " + classInfo.getClassAndPackage() + ".class)");
            List<? extends TypeMirror> typeArguments = classInfo.getTypeArguments();
            if (typeArguments == null || typeArguments.isEmpty()) {
                builder.addStatement("return (TypeAdapter<T>) new " + classInfo.getTypeAdapterQualifiedClassName() + "(gson)");
            } else {
                builder.addStatement("java.lang.reflect.Type parameters = type.getType()");
                builder.beginControlFlow("if (parameters instanceof java.lang.reflect.ParameterizedType)");
                builder.addStatement(
                        "java.lang.reflect.ParameterizedType parameterizedType = (java.lang.reflect.ParameterizedType) parameters");
                builder.addStatement(
                        "java.lang.reflect.Type[] parametersType = parameterizedType.getActualTypeArguments()");
                String statement = "return (TypeAdapter<T>) new " + classInfo.getTypeAdapterQualifiedClassName() + "(gson";
                for (int idx = 0; idx < typeArguments.size(); idx++) {
                    statement += ", parametersType[" + idx + "]";
                }
                statement += ")";
                builder.addStatement(statement);
                builder.endControlFlow();
                builder.beginControlFlow("else");
                builder.addStatement("TypeToken objectToken = TypeToken.get(Object.class)");
                statement = "return (TypeAdapter<T>) new " + classInfo.getTypeAdapterQualifiedClassName() + "(gson";
                for (int idx = 0; idx < typeArguments.size(); idx++) {
                    statement += ", objectToken.getType()";
                }
                statement += ")";
                builder.addStatement(statement);
                builder.endControlFlow();
            }
            builder.endControlFlow();
        }

        builder.addCode("return null;\n");

        return builder.build();
    }
}

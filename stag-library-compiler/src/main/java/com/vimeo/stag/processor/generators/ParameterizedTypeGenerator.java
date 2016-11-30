package com.vimeo.stag.processor.generators;

import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;
import com.vimeo.stag.processor.utils.FileGenUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

public class ParameterizedTypeGenerator {

    static final String CLASS_NAME = "ParameterizedTypeUtil";
    private static final String PARAMETERIZED_CLASS_NAME = "ParameterizedTypeImpl";

    @NotNull
    private static MethodSpec generateParameterizedTypeWithOwnerMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("newParameterizedTypeWithOwner")
                .returns(ParameterizedType.class)
                .addParameter(Type.class, "ownerType")
                .addParameter(Type.class, "rawType")
                .addParameter(TypeVariableName.get("Type..."), "typeArguments")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        builder.addCode("return new ParameterizedTypeImpl(ownerType, rawType, typeArguments);\n");
        return builder.build();
    }

    @NotNull
    private static MethodSpec generateTypeTokenGetterMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getTypeToken")
                .returns(TypeToken.class)
                .addParameter(Type.class, "rawType")
                .addParameter(TypeVariableName.get("Type..."), "typeArguments")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);
        builder.addStatement("return TypeToken.get(new ParameterizedTypeImpl(null, rawType, typeArguments))");
        return builder.build();
    }

    @NotNull
    private static MethodSpec generateCheckArgumentMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("checkArgument")
                .addParameter(boolean.class, "condition")
                .addModifiers(Modifier.STATIC)
                .returns(void.class);

        builder.beginControlFlow(" if (!condition)");
        builder.addStatement("throw new IllegalArgumentException()");
        builder.endControlFlow();
        return builder.build();
    }

    @NotNull
    private static MethodSpec generateCheckNotPrimitiveMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("checkNotPrimitive")
                .addParameter(Type.class, "type")
                .addModifiers(Modifier.STATIC)
                .returns(void.class);
        builder.addCode("checkArgument(!(type instanceof Class<?>) || !((Class<?>) type).isPrimitive());\n");
        return builder.build();
    }

    @NotNull
    private static MethodSpec generateCheckNotNullMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("checkNotNull")
                .addParameter(TypeVariableName.get("T"), "obj")
                .addModifiers(Modifier.STATIC)
                .addTypeVariable(TypeVariableName.get("T"))
                .returns(TypeVariableName.get("T"));
        builder.beginControlFlow("if (obj == null)");
        builder.addStatement("throw new NullPointerException()");
        builder.endControlFlow();
        builder.addStatement("return obj");
        return builder.build();
    }

    @NotNull
    private static MethodSpec generateCanonicalizeMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("canonicalize")
                .addModifiers(Modifier.STATIC)
                .addParameter(Type.class, "type")
                .returns(Type.class);

        builder.beginControlFlow("if (type instanceof Class)");
        builder.addStatement("Class<?> c = (Class<?>) type");
        builder.addStatement("return c");
        builder.endControlFlow();

        builder.beginControlFlow("if (type instanceof ParameterizedType)");
        builder.addStatement("ParameterizedType p = (ParameterizedType) type");
        builder.addStatement("return new ParameterizedTypeImpl(p.getOwnerType(), p.getRawType(), p.getActualTypeArguments())");
        builder.endControlFlow();

        builder.addStatement("return null");
        return builder.build();
    }

    @NotNull
    private static TypeSpec generateParameterizedTypeImpl() {
        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(PARAMETERIZED_CLASS_NAME)
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .addSuperinterface(ParameterizedType.class)
                .addSuperinterface(Serializable.class)
                .addField(Type.class, "ownerType", Modifier.FINAL, Modifier.PRIVATE)
                .addField(Type.class, "rawType", Modifier.FINAL, Modifier.PRIVATE)
                .addField(Type[].class, "typeArguments", Modifier.FINAL, Modifier.PRIVATE);

        FieldSpec fieldSpec = FieldSpec.builder(long.class, "serialVersionUID")
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL, Modifier.STATIC)
                .initializer("0").build();

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(Type.class, "ownerType")
                .addParameter(Type.class, "rawType")
                .addParameter(Type[].class, "typeArguments");

        constructorBuilder.beginControlFlow("if (rawType instanceof Class<?>)");
        constructorBuilder.addStatement("Class<?> rawTypeAsClass = (Class<?>) rawType");
        constructorBuilder.addStatement("boolean isStaticOrTopLevelClass = java.lang.reflect.Modifier.isStatic(rawTypeAsClass.getModifiers()) " +
                "|| rawTypeAsClass.getEnclosingClass() == null");
        constructorBuilder.addStatement("checkArgument(ownerType != null || isStaticOrTopLevelClass)");
        constructorBuilder.endControlFlow();
        constructorBuilder.addCode(
                "this.ownerType = ownerType == null ? null : canonicalize(ownerType);\n" +
                        "this.rawType = canonicalize(rawType);\n" +
                        "this.typeArguments = typeArguments.clone();\n" +
                        "for (int t = 0; t < this.typeArguments.length; t++) {\n" +
                        "\tcheckNotNull(this.typeArguments[t]);\n" +
                        "\tcheckNotPrimitive(this.typeArguments[t]);\n" +
                        "\tthis.typeArguments[t] = canonicalize(this.typeArguments[t]);\n" +
                        "}\n");

        MethodSpec.Builder ownerTypeMethod = MethodSpec.methodBuilder("getOwnerType")
                .returns(Type.class)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return ownerType");

        MethodSpec.Builder rawTypeMethod = MethodSpec.methodBuilder("getRawType")
                .returns(Type.class)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return rawType");

        MethodSpec.Builder typeArgumentMethod = MethodSpec.methodBuilder("getActualTypeArguments")
                .returns(Type[].class)
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return typeArguments.clone()");

        adapterBuilder.addField(fieldSpec);
        adapterBuilder.addMethod(constructorBuilder.build());
        adapterBuilder.addMethod(ownerTypeMethod.build());
        adapterBuilder.addMethod(rawTypeMethod.build());
        adapterBuilder.addMethod(typeArgumentMethod.build());
        return adapterBuilder.build();
    }

    public static void generateParameterizedUtilClass(Filer filer, String packageName) throws IOException {

        TypeSpec.Builder adapterBuilder = TypeSpec.classBuilder(CLASS_NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addType(generateParameterizedTypeImpl())
                .addMethod(generateCheckArgumentMethod())
                .addMethod(generateParameterizedTypeWithOwnerMethod())
                .addMethod(generateCanonicalizeMethod())
                .addMethod(generateCheckNotPrimitiveMethod())
                .addMethod(generateTypeTokenGetterMethod())
                .addMethod(generateCheckNotNullMethod());

        JavaFile javaFile = JavaFile.builder(packageName, adapterBuilder.build()).build();
        FileGenUtils.writeToFile(javaFile, filer);
    }
}
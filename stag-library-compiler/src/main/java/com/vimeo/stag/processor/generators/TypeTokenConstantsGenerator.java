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


import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class TypeTokenConstantsGenerator {

    private static final String CLASS_STAG_TYPE_TOKEN_CONSTANTS = "StagTypeTokenConstants";

    private static final String FIELD_PREFIX = "TYPE_TOKEN_";

    @NotNull
    private final HashMap<String, TypeTokenInfo> mTypesToBeGenerated = new HashMap<>();
    @NotNull
    private final String mGeneratedPackageName;

    public TypeTokenConstantsGenerator(@NotNull String generatedPackageName) {
        mGeneratedPackageName = generatedPackageName;
    }

    @NotNull
    private static MethodSpec generateTypeTokenGetters(@NotNull String name, @NotNull TypeName typeName) {
        MethodSpec.Builder mBuilder = MethodSpec.methodBuilder(getMethodName(name))
                .returns(ParameterizedTypeName.get(ClassName.get(TypeToken.class), typeName))
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .beginControlFlow("if (" + name + " == null)")
                .addStatement(name + " = new com.google.gson.reflect.TypeToken<" + typeName + ">(){}")
                .endControlFlow()
                .addStatement("return " + name);
        return mBuilder.build();
    }

    @NotNull
    private static String getMethodName(@NotNull String name) {
        return "get" + name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    /**
     * Add the type token to be generated
     *
     * @param type TypeMirror
     * @return String
     */
    @NotNull
    String addTypeToken(@NotNull TypeMirror type) {
        String typeString = type.toString();
        TypeTokenInfo typeTokenInfo = mTypesToBeGenerated.get(typeString);
        if (null == typeTokenInfo) {
            typeTokenInfo = new TypeTokenInfo();
            typeTokenInfo.mTypeMirror = type;
            typeTokenInfo.mFieldName = FIELD_PREFIX + mTypesToBeGenerated.size();
            typeTokenInfo.mMethodName = getMethodName(typeTokenInfo.mFieldName) + "()";
            mTypesToBeGenerated.put(typeString, typeTokenInfo);
        }

        return mGeneratedPackageName + "." + CLASS_STAG_TYPE_TOKEN_CONSTANTS + "." + typeTokenInfo.mMethodName;
    }

    /**
     * Generates the public API in the form of the {@code Stag.Factory} type adapter factory
     * for the annotated classes. Creates the spec for the class.
     *
     * @return A non null TypeSpec for the type token constants class.
     */
    public TypeSpec createTypeTokenConstantsSpec() {
        TypeSpec.Builder typeTokenConstantsBuilder = TypeSpec.classBuilder(CLASS_STAG_TYPE_TOKEN_CONSTANTS)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        for (Map.Entry<String, TypeTokenInfo> entry : mTypesToBeGenerated.entrySet()) {
            TypeTokenInfo typeTokenInfo = entry.getValue();
            TypeName typeName = TypeVariableName.get(typeTokenInfo.mTypeMirror);
            TypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(TypeToken.class), typeName);

            FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(parameterizedTypeName, typeTokenInfo.mFieldName, Modifier.PUBLIC, Modifier.STATIC);
            typeTokenConstantsBuilder.addField(fieldSpecBuilder.build());
            typeTokenConstantsBuilder.addMethod(generateTypeTokenGetters(typeTokenInfo.mFieldName, typeName));
        }

        return typeTokenConstantsBuilder.build();
    }

    private static class TypeTokenInfo {

        TypeMirror mTypeMirror;
        String mFieldName;
        String mMethodName;

        TypeTokenInfo() {
        }
    }
}
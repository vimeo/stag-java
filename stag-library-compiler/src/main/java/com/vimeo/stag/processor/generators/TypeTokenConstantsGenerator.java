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
import com.vimeo.stag.processor.utils.FileGenUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class TypeTokenConstantsGenerator {

    private static final String CLASS_STAG_TYPE_TOKEN_CONSTANTS = "StagTypeTokenConstants";

    private static final String FIELD_PREFIX = "TYPE_TOKEN_";

    @NotNull
    private final Filer mFiler;

    @NotNull
    private final HashMap<TypeMirror, String> mTypesToBeGenerated = new HashMap<>();

    @NotNull
    private final String mGeneratedPackageName;

    public TypeTokenConstantsGenerator(@NotNull Filer filer, @NotNull String generatedPackageName) {
        mFiler = filer;
        mGeneratedPackageName = generatedPackageName;
    }

    public String addTypeToken(@NotNull TypeMirror type) {
        String result = mTypesToBeGenerated.get(type);
        if(null == result) {
            result = FIELD_PREFIX + mTypesToBeGenerated.size();
            mTypesToBeGenerated.put(type, result);
        }
        return mGeneratedPackageName + "." + CLASS_STAG_TYPE_TOKEN_CONSTANTS + "." + result;
    }


    /**
     * Generates the public API in the form of the {@code Stag.Factory} type adapter factory
     * for the annotated classes.
     *
     * @throws IOException throws an exception
     *                     if we are unable to write the file
     *                     to the filesystem.
     */
    public void generateTypeTokenConstants() throws IOException {
        if(!mTypesToBeGenerated.isEmpty()) {
            TypeSpec.Builder adaptersBuilder =
                    TypeSpec.classBuilder(CLASS_STAG_TYPE_TOKEN_CONSTANTS).addModifiers(Modifier.PUBLIC, Modifier.FINAL);

            for(Map.Entry<TypeMirror, String > entry : mTypesToBeGenerated.entrySet()) {
                TypeMirror type = entry.getKey();
                TypeName typeName = TypeVariableName.get(type);
                TypeName parameterizedTypeName =  ParameterizedTypeName.get(ClassName.get(TypeToken.class), typeName);
                FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(parameterizedTypeName, entry.getValue(), Modifier.STATIC, Modifier.FINAL, Modifier.PUBLIC);
                fieldSpecBuilder.initializer("new com.google.gson.reflect.TypeToken<" + typeName + ">(){}");
                adaptersBuilder.addField(fieldSpecBuilder.build());
            }

            JavaFile javaFile =
                    JavaFile.builder(mGeneratedPackageName, adaptersBuilder.build()).build();

            FileGenUtils.writeToFile(javaFile, mFiler);
        }
    }



}

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

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public abstract class AdapterGenerator {

    /**
     * Gets the JSON name for the element the name passed to
     * {@link SerializedName} will be used. If the element is
     * not annotated with {@link SerializedName}, the variable
     * name is used.
     *
     * @param element the element to get the name for.
     * @return a non null string to use as the JSON key.
     */
    @NotNull
    static String getJsonName(@NotNull Element element) {

        String name = null != element.getAnnotation(SerializedName.class) ?
                element.getAnnotation(SerializedName.class).value() :
                null;

        if (null == name || name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        return name;
    }

    /**
     * Returns the alternate names for the {@link Element}.
     *
     * @param element the element to check for alternate names.
     * @return an array of alternate names, or null if there are none.
     */
    @Nullable
    static String[] getAlternateJsonNames(@NotNull Element element) {
        return null != element.getAnnotation(SerializedName.class) ?
                element.getAnnotation(SerializedName.class).alternate() :
                null;
    }

    /**
     * Creates a {@link TypeSpec} for the the type adapter.
     *
     * @param stagGenerator the generator for the Stag class.
     * @return A non null {@link TypeSpec} describing the adapter class.
     */
    @NotNull
    public abstract TypeSpec createTypeAdapterSpec(@NotNull StagGenerator stagGenerator);

    /**
     * Creates a TypeToken field in the generated adapter factory
     *
     * @param typeMirror Type of class for which TypeToken has to be generated
     * @return {@link FieldSpec}
     */
    @NotNull
    static FieldSpec createTypeTokenSpec(@NotNull TypeMirror typeMirror) {
        ParameterizedTypeName typeTokenType = ParameterizedTypeName.get(ClassName.get(TypeToken.class), TypeVariableName.get(typeMirror));
        FieldSpec.Builder typeTokenBuilder = FieldSpec.builder(typeTokenType, "TYPE_TOKEN", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
        typeTokenBuilder.initializer("TypeToken.get(" + typeMirror.toString() + ".class)");
        return typeTokenBuilder.build();
    }
}

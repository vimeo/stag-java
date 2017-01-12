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
import com.squareup.javapoet.TypeSpec;
import com.vimeo.stag.GsonAdapterKey;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;

public abstract class AdapterGenerator {

    /**
     * If the element is not annotated with {@link SerializedName} or {@link GsonAdapterKey}, the variable name is used.
     * If both of them are used we will give preference to GsonAdapterKey
     */
    @NotNull
    static String getJsonName(@NotNull Element element) {

        String name = (null != element.getAnnotation(GsonAdapterKey.class)) ?
                element.getAnnotation(GsonAdapterKey.class).value() : null;


        if (null == name || name.isEmpty()) {
            name = (null != element.getAnnotation(SerializedName.class)) ?
                    element.getAnnotation(SerializedName.class).value() : null;
        }

        if (null == name || name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        return name;
    }

    /**
     * Returns the alternate name for the {@link Element}
     */
    @Nullable
    static String[] getAlternateJsonNames(@NotNull Element element) {
        return (null != element.getAnnotation(SerializedName.class)) ? element.getAnnotation(SerializedName.class).alternate() : null;
    }

    @NotNull
    public abstract TypeSpec getTypeAdapterSpec(@NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator, @NotNull StagGenerator stagGenerator);
}
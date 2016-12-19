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
package com.vimeo.stag.processor.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public final class ElementUtils {

    private static Elements sElementUtils;

    private ElementUtils() {
        throw new UnsupportedOperationException("This class is not instantiable");
    }

    public static void initialize(@NotNull Elements elementUtils) {
        sElementUtils = elementUtils;
    }

    @NotNull
    private static Elements getUtils() {
        Preconditions.checkNotNull(sElementUtils);
        return sElementUtils;
    }

    @Nullable
    public static TypeMirror getTypeFromQualifiedName(@NotNull String qualifiedName) {
        Elements elements = ElementUtils.getUtils();
        TypeElement typeElement = elements.getTypeElement(qualifiedName);
        return typeElement.asType();
    }

    @NotNull
    public static String getPackage(@NotNull TypeMirror type) {
        Element element = TypeUtils.getUtils().asElement(type);
        PackageElement packageElement = sElementUtils.getPackageOf(element);
        return packageElement.getQualifiedName().toString();
    }

    public static boolean isEnum(@Nullable Element element) {
        return element != null && element.getKind() == ElementKind.ENUM;
    }
}

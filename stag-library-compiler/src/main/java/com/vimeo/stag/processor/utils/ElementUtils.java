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

import com.vimeo.stag.UseStag;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public final class ElementUtils {

    @Nullable
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
        TypeElement typeElement = getTypeElementFromQualifiedName(qualifiedName);
        return null != typeElement ? typeElement.asType() : null;
    }

    @Nullable
    public static TypeElement getTypeElementFromQualifiedName(@NotNull String qualifiedName) {
        Elements elements = ElementUtils.getUtils();
        return elements.getTypeElement(qualifiedName);
    }

    @NotNull
    public static String getPackage(@NotNull TypeMirror type) {
        Element element = TypeUtils.getElementFromTypeMirror(type);
        PackageElement packageElement = getUtils().getPackageOf(element);
        return packageElement.getQualifiedName().toString();
    }

    /**
     * Get the annotation applied of the specified type if it has been applied to the provided
     * element.  This method will check the supplied element and work up the type hierarchy,
     * returning the first annotation instance found.
     *
     * @param annotationType annotation type to search for
     * @param element element to analyze
     * @param <T> annotation type
     * @return annotation instance or {@code null} if not found
     */
    @Nullable
    public static <T extends Annotation> T findAnnotation(
            @NotNull Class<T> annotationType, @NotNull Element element) {
        T annotatedElement = element.getAnnotation(annotationType);
        if (annotatedElement != null)
            return annotatedElement;

        if (element instanceof TypeElement) {
            TypeElement typeElement = (TypeElement) element;
            TypeMirror superTypeMirror = typeElement.getSuperclass();
            if (superTypeMirror != null) {
                Element superTypeElement = TypeUtils.getElementFromTypeMirror(superTypeMirror);
                return superTypeElement == null ? null : findAnnotation(annotationType, superTypeElement);
            }
        }

        return null;
    }

    /**
     * Determines if an element is a supported type.
     *
     * @param element the element to check.
     * @return true if the element is supported
     * (class or enum), false otherwise.
     */
    public static boolean isSupportedElementKind(@Nullable Element element) {
        if (element == null) {
            return false;
        }
        ElementKind elementKind = element.getKind();
        return (elementKind == ElementKind.CLASS || elementKind == ElementKind.ENUM)
                && findAnnotation(UseStag.class, element) != null;
    }

}

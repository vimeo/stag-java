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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public final class ElementUtils {

    @Nullable private static Elements sElementUtils;

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
        return typeElement != null ? typeElement.asType() : null;
    }

    @Nullable
    public static TypeMirror getTypeFromClass(@NotNull Class clazz) {
        return getTypeFromQualifiedName(clazz.getName());
    }

    @Nullable
    public static TypeElement getTypeElementFromQualifiedName(@NotNull String qualifiedName) {
        Elements elements = ElementUtils.getUtils();
        return elements.getTypeElement(qualifiedName);
    }

    @NotNull
    public static String getPackage(@NotNull TypeMirror type) {
        Element element = TypeUtils.unsafeTypeMirrorToTypeElement(type);
        PackageElement packageElement = getUtils().getPackageOf(element);
        return packageElement.getQualifiedName().toString();
    }

    /**
     * Determines whether or not the element provided is annotated with the annotation type
     * specified.
     *
     * @param annotationClass annotation class to search for
     * @param element         element to query
     * @param <T>             annotation type
     * @return {@code true} if the element is annotated, {@code false} otherwise
     */
    private static <T extends Annotation> boolean isAnnotatedWith(@NotNull Class<T> annotationClass,
                                                                  @Nullable Element element) {
        return element != null && element.getAnnotation(annotationClass) != null;
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
               && isAnnotatedWith(UseStag.class, element);
    }

    @Nullable
    public static ExecutableElement getFirstConstructor(@Nullable TypeMirror typeMirror) {
        Element typeElement = typeMirror != null ? TypeUtils.unsafeTypeMirrorToTypeElement(typeMirror) : null;
        if (typeElement != null) {
            for (Element element : typeElement.getEnclosedElements()) {
                if (element instanceof ExecutableElement) {
                    ExecutableElement executableElement = ((ExecutableElement) element);
                    Name name = executableElement.getSimpleName();
                    if (name.contentEquals("<init>")) {
                        return executableElement;
                    }
                }
            }
        }

        return null;
    }
}

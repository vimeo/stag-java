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
package com.vimeo.stag.processor;

import com.vimeo.stag.processor.dummy.DummyGenericClass;
import com.vimeo.stag.processor.utils.Preconditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.junit.Assert.assertTrue;

final class Utils {

    @Nullable private static Elements elements;
    @Nullable private static Types types;

    private Utils() {
    }

    @NotNull
    private static Elements safeElements() {
        Preconditions.checkNotNull(elements);
        return elements;
    }

    @NotNull
    private static Types safeTypes() {
        Preconditions.checkNotNull(types);
        return types;
    }

    public static void setup(@NotNull Elements elements, @NotNull Types types) {
        Preconditions.checkNotNull(elements);
        Preconditions.checkNotNull(types);

        Utils.elements = elements;
        Utils.types = types;
    }

    public static <T> void testZeroArgumentConstructorFinalClass(Class<T> clazz) throws Exception {
        boolean exceptionThrown = false;
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                exceptionThrown = true;
            }
        }
        assertTrue(exceptionThrown);
    }

    /**
     * Gets the parameterized class with the given parameters.
     *
     * @param clazz      the class to parameterize.
     * @param parameters the parameters to use.
     * @return the declared type mirror with the correct type parameters.
     */
    @NotNull
    public static DeclaredType getParameterizedClass(@NotNull Class clazz, @NotNull Class... parameters) {
        TypeElement rootType = safeElements().getTypeElement(clazz.getName());
        TypeMirror[] params = new TypeMirror[parameters.length];

        for (int n = 0; n < parameters.length; n++) {
            params[n] = safeElements().getTypeElement(parameters[n].getName()).asType();
        }

        return safeTypes().getDeclaredType(rootType, params);
    }

    @Nullable
    public static TypeElement getElementFromClass(@NotNull Class clazz) {
        return safeElements().getTypeElement(clazz.getName());
    }

    @Nullable
    public static TypeMirror getTypeMirrorFromClass(@NotNull Class clazz) {
        TypeElement element = getElementFromClass(clazz);
        return element != null ? element.asType() : null;
    }

    @Nullable
    public static TypeElement getElementFromObject(@NotNull Object object) {
        return safeElements().getTypeElement(object.getClass().getName());
    }

    @Nullable
    public static TypeMirror getTypeMirrorFromObject(@NotNull Object object) {
        TypeElement element = getElementFromObject(object);
        return element != null ? element.asType() : null;
    }

    @NotNull
    public static TypeMirror getGenericVersionOfClass(@NotNull Class clazz) {
        List<? extends TypeParameterElement> params =
                safeElements().getTypeElement(clazz.getName()).getTypeParameters();
        TypeMirror[] genericTypes = new TypeMirror[params.size()];
        for (int n = 0; n < genericTypes.length; n++) {
            genericTypes[n] = params.get(n).asType();
        }
        return safeTypes().getDeclaredType(safeElements().getTypeElement(DummyGenericClass.class.getName()),
                                           genericTypes);
    }

}

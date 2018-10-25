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
package com.vimeo.stag.processor.generators.model;

import com.vimeo.stag.UseStag.FieldOption;
import com.vimeo.stag.processor.generators.model.accessor.MethodFieldAccessor.Notation;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.lang.model.type.TypeMirror;

public final class SupportedTypesModel {

    @NotNull private final Map<String, AnnotatedClass> mSupportedTypesMap = new LinkedHashMap<>();
    @NotNull private final Map<String, AnnotatedClass> mKnownInheritedTypesMap = new LinkedHashMap<>();
    @NotNull private final Notation mNamingNotation;

    public SupportedTypesModel(@NotNull Notation namingNotation) {
        mNamingNotation = namingNotation;
    }

    /**
     * Tells the model that we support this type.
     *
     * @param object the annotated class that we
     *               should track.
     */
    private void addSupportedType(@NotNull AnnotatedClass object) {
        mSupportedTypesMap.put(TypeUtils.getOuterClassType(object.getType()), object);
    }

    /**
     * Retrieves the AnnotatedClass object for the
     * specific TypeMirror.
     *
     * @param type the type that maps to a specific
     *             AnnotatedClass.
     * @return the AnnotatedClass object associated
     * with the class type.
     */
    @Nullable
    public AnnotatedClass getSupportedType(@NotNull TypeMirror type) {
        return getSupportedType(TypeUtils.getOuterClassType(type));
    }

    /**
     * Retrieves the AnnotatedClass object for the
     * specific TypeMirror.
     *
     * @param type the type that maps to a specific
     *             AnnotatedClass.
     * @return the AnnotatedClass object associated
     * with the class type.
     */
    @Nullable
    private AnnotatedClass getSupportedType(@NotNull String type) {
        return mSupportedTypesMap.get(type);
    }

    /**
     * Adds an AnnotatedClass object for the
     * specific TypeMirror if it does not exist in the list of
     * known inherited types
     *
     * @param type the type that maps to a specific
     *             AnnotatedClass.
     * @return the AnnotatedClass object associated
     * with the class type.
     */
    @NotNull
    public AnnotatedClass addToKnownInheritedType(@NotNull TypeMirror type, @Nullable FieldOption childFieldOption) {
        String outerClassType = TypeUtils.getOuterClassType(type);
        AnnotatedClass model = getSupportedType(outerClassType);
        if (model == null) {
            model = mKnownInheritedTypesMap.get(outerClassType);
            if (model == null) {
                model = new AnnotatedClass(this, TypeUtils.safeTypeMirrorToTypeElement(type), mNamingNotation, childFieldOption);
                mKnownInheritedTypesMap.put(outerClassType, model);
            }
        }
        return model;
    }

    /**
     * Adds an AnnotatedClass object for the
     * specific TypeMirror if it does not exist.
     *
     * @param type the type that maps to a specific
     *             AnnotatedClass.
     */
    public void addSupportedType(@NotNull TypeMirror type) {
        String outerClassType = TypeUtils.getOuterClassType(type);
        AnnotatedClass model = getSupportedType(outerClassType);

        if (model == null) {
            model = mKnownInheritedTypesMap.get(outerClassType);
            if (model == null) {
                model = new AnnotatedClass(this, TypeUtils.safeTypeMirrorToTypeElement(type), mNamingNotation);
            }
            addSupportedType(model);
        }
    }

    /**
     * A set of all supported elements (these map 1 to 1
     * to an AnnotatedClass). This may return both generic
     * and concrete types.
     *
     * @return the set of supported types.
     */
    @NotNull
    public Collection<AnnotatedClass> getSupportedTypes() {
        return mSupportedTypesMap.values();
    }

}

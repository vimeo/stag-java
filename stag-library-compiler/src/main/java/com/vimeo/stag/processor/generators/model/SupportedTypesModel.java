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

import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public final class SupportedTypesModel {

    private SupportedTypesModel() {
    }

    @Nullable
    private static SupportedTypesModel sInstance;

    public static synchronized SupportedTypesModel getInstance() {
        if (sInstance == null) {
            sInstance = new SupportedTypesModel();
        }

        return sInstance;
    }

    private final Map<String, AnnotatedClass> mSupportedTypesMap = new HashMap<>();
    private final Set<Element> mSupportedTypes = new HashSet<>();

    /**
     * Tells the model that we support this type.
     *
     * @param object the annotated class that we
     *               should track.
     */
    public void addSupportedType(@NotNull AnnotatedClass object) {
        mSupportedTypesMap.put(TypeUtils.getOuterClassType(object.getType()), object);
        mSupportedTypes.add(object.getElement());
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
    @NotNull
    public AnnotatedClass getSupportedType(@NotNull TypeMirror type) {
        AnnotatedClass model = mSupportedTypesMap.get(TypeUtils.getOuterClassType(type));

        if (model == null) {
            model = new AnnotatedClass(TypeUtils.getUtils().asElement(type),
                                       new ArrayList<VariableElement>());
            addSupportedType(model);
        }
        return model;
    }

    /**
     * A list of all supported AnnotatedClasses.
     * This may return both generic and concrete
     * types.
     *
     * @return the list of all supported types.
     */
    @NotNull
    public List<AnnotatedClass> getSupportedTypes() {
        return new ArrayList<>(mSupportedTypesMap.values());
    }

    /**
     * A set of all supported elements (these map 1 to 1
     * to an AnnotatedClass). This may return both generic
     * and concrete types.
     *
     * @return the set of supported types.
     */
    @NotNull
    public Set<Element> getSupportedElements() {
        return new HashSet<>(mSupportedTypes);
    }

}

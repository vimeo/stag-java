/**
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

import com.vimeo.stag.processor.StagProcessor;
import com.vimeo.stag.processor.utils.DebugLog;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class AnnotatedClass {

    private static final String TAG = AnnotatedClass.class.getSimpleName();

    @NotNull
    private final TypeMirror mType;

    @NotNull
    private final Element mElement;

    @NotNull
    private final List<VariableElement> mMemberVariables;

    @Nullable
    private final TypeMirror mInheritedType;

    public AnnotatedClass(@NotNull Element element, @NotNull List<VariableElement> members) {
        mType = element.asType();
        mElement = element;
        mInheritedType = TypeUtils.getInheritedType(element);
        mMemberVariables = new ArrayList<>(members);
    }

    @NotNull
    public TypeMirror getType() {
        return mType;
    }

    @NotNull
    public Element getElement() {
        return mElement;
    }

    /**
     * Returns a map of the member variables (Elements) to
     * their concrete types (TypeMirror). If the type of this
     * AnnotatedClass is generic, then the concrete types
     * will be made as concrete as possible based on the type
     * parameters of this class. If an Element was already
     * not generic, then the TypeMirror for that element
     * will contain the same exact type.
     *
     * @return a valid map of element to their un-generified
     * types.
     */
    @NotNull
    public Map<Element, TypeMirror> getMemberVariables() {
        Map<Element, TypeMirror> map = new HashMap<>();
        for (VariableElement element : mMemberVariables) {
            map.put(element, element.asType());
        }

        DebugLog.log(TAG, "getMemberVariables() - " + mType.toString());

        if (mInheritedType != null) {
            DebugLog.log(TAG, "\t\tInherited Type - " + mInheritedType.toString());

            AnnotatedClass genericInheritedType =
                    SupportedTypesModel.getInstance().getSupportedType(mInheritedType);

            map.putAll(TypeUtils.getConcreteMembers(mInheritedType, genericInheritedType.getElement(),
                                                    genericInheritedType.getMemberVariables()));
        }

        if (StagProcessor.DEBUG) {
            for (Entry<Element, TypeMirror> entry : map.entrySet()) {
                DebugLog.log(TAG, "\t\tMember variables - " + entry.toString());
            }
        }

        return map;
    }

}

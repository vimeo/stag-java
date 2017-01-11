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

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.UseStag;
import com.vimeo.stag.processor.StagProcessor;
import com.vimeo.stag.processor.utils.DebugLog;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class AnnotatedClass {

    private static final String TAG = AnnotatedClass.class.getSimpleName();

    @NotNull
    private final TypeMirror mType;

    @NotNull
    private final Element mElement;

    @NotNull
    private final Map<Element, TypeMirror> mMemberVariables;

    private List<Element> mNestedElements;

    AnnotatedClass(@NotNull Element element) {
        mType = element.asType();
        mElement = element;
        TypeMirror inheritedType = TypeUtils.getInheritedType(element);

        UseStag useStag = element.getAnnotation(UseStag.class);
        int fieldOptions = useStag == null ? UseStag.FIELD_OPTION_ALL : useStag.value();

        mMemberVariables = new HashMap<>();
        for (Element enclosedElement : element.getEnclosedElements()) {
            addToSupportedTypes(enclosedElement, fieldOptions);
        }

        if (inheritedType != null) {
            DebugLog.log(TAG, "\t\tInherited Type - " + inheritedType.toString());

            AnnotatedClass genericInheritedType =
                    SupportedTypesModel.getInstance().getSupportedType(inheritedType);

            mMemberVariables.putAll(TypeUtils.getConcreteMembers(inheritedType, genericInheritedType.getElement(),
                    genericInheritedType.getMemberVariables()));
        }

    }

    //This is to avoid the infinite recursive loop where an inner class can be deriving for this class itself
    void initNestedClasses(){
        if(null != mNestedElements) {
            for (Element element: mNestedElements) {
                SupportedTypesModel.getInstance().getSupportedType(element.asType());
            }
        }
    }

    private static void checkModifiers(VariableElement variableElement, Set<Modifier> modifiers) {
        if (!modifiers.contains(Modifier.STATIC)) {
            if (modifiers.contains(Modifier.FINAL)) {
                throw new RuntimeException("Unable to access field \"" +
                        variableElement.getSimpleName().toString() + "\" in class " +
                        variableElement.getEnclosingElement().asType() +
                        ", field must not be final.");
            } else if (modifiers.contains(Modifier.PRIVATE)) {
                throw new RuntimeException("Unable to access field \"" +
                        variableElement.getSimpleName().toString() + "\" in class " +
                        variableElement.getEnclosingElement().asType() +
                        ", field must not be private.");
            }
        }
    }

    private void addToSupportedTypes(@NotNull Element element, int fieldOptions) {
        if (element instanceof VariableElement) {
            if(shouldIncludeField(element, fieldOptions)) {
                final VariableElement variableElement = (VariableElement) element;
                Set<Modifier> modifiers = variableElement.getModifiers();
                if (!modifiers.contains(Modifier.FINAL) && !modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.TRANSIENT)) {
                    checkModifiers(variableElement, modifiers);
                    if (!TypeUtils.isAbstract(element)) {
                        SupportedTypesModel.getInstance().checkAndAddExternalAdapter(variableElement);
                    }
                    if (StagProcessor.DEBUG) {
                        DebugLog.log(TAG, "\t\tMember variables - " + variableElement.asType().toString());
                    }
                    mMemberVariables.put(variableElement, variableElement.asType());
                }
            }
        } else if (element instanceof TypeElement) {
            if(null == mNestedElements) {
                mNestedElements = new ArrayList<>();
            }
            mNestedElements.add(element);
        }
    }

    private boolean shouldIncludeField(@NotNull Element element, int fieldOption) {
        switch (fieldOption){
            case UseStag.FIELD_OPTION_NONE:
                return false;
            case UseStag.FIELD_OPTION_SERIALIZED_NAME:
                return element.getAnnotation(SerializedName.class) != null || element.getAnnotation(GsonAdapterKey.class) != null;
            case UseStag.FIELD_OPTION_ALL:
                return true;
            default:
                throw new RuntimeException("Unknown field option provided for class " + mElement.asType());
        }
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
        return new HashMap<>(mMemberVariables);
    }
}
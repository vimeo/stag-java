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
import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;
import com.vimeo.stag.processor.generators.model.accessor.DirectFieldAccessor;
import com.vimeo.stag.processor.generators.model.accessor.FieldAccessor;
import com.vimeo.stag.processor.utils.DebugLog;
import com.vimeo.stag.processor.utils.MessagerUtils;
import com.vimeo.stag.processor.utils.Preconditions;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

public class AnnotatedClass {

    @NotNull
    public static Set<TypeMirror> annotatedClassToTypeMirror(@NotNull Collection<AnnotatedClass> annotatedClasses) {
        Set<TypeMirror> typeMirrors = new HashSet<>();
        for (AnnotatedClass annotatedClass : annotatedClasses) {
            typeMirrors.add(annotatedClass.getType());
        }

        return typeMirrors;
    }

    private static final String TAG = AnnotatedClass.class.getSimpleName();

    @NotNull private final TypeMirror mType;
    @NotNull private final TypeElement mElement;
    @NotNull private final LinkedHashMap<FieldAccessor, TypeMirror> mMemberVariables;
    @NotNull private final SupportedTypesModel mSupportedTypesModel;

    AnnotatedClass(@NotNull SupportedTypesModel supportedTypesModel, @NotNull TypeElement element) {
        this(supportedTypesModel, element, null);
    }

    AnnotatedClass(@NotNull SupportedTypesModel supportedTypesModel, @NotNull TypeElement element, @Nullable FieldOption childFieldOption) {
        mSupportedTypesModel = supportedTypesModel;
        mType = element.asType();
        mElement = element;
        Map<String, FieldAccessor> variableNames = new HashMap<>(element.getEnclosedElements().size());
        TypeMirror inheritedType = TypeUtils.getInheritedType(element);

        UseStag useStag = element.getAnnotation(UseStag.class);

        FieldOption fieldOption = useStag != null ? useStag.value() : null;
        if (fieldOption == null) {
            useStag = element.getAnnotation(UseStag.class);

            // The field option can be null if the inherited class was not annotated
            fieldOption = useStag != null ? useStag.value() : childFieldOption;
        }

        Preconditions.checkNotNull(fieldOption);

        mMemberVariables = new LinkedHashMap<>();

        if (inheritedType != null) {
            DebugLog.log(TAG, "\t\tInherited Type - " + inheritedType.toString());

            AnnotatedClass genericInheritedType = mSupportedTypesModel.addToKnownInheritedType(inheritedType, fieldOption);

            LinkedHashMap<FieldAccessor, TypeMirror> inheritedMemberVariables = TypeUtils.getConcreteMembers(inheritedType,
                                                                                                       genericInheritedType.getElement(),
                                                                                                       genericInheritedType.getMemberVariables());

            for (Map.Entry<FieldAccessor, TypeMirror> entry : inheritedMemberVariables.entrySet()) {
                addMemberVariable(entry.getKey(), entry.getValue(), variableNames);
            }
        }

        if (!TypeUtils.isEnum(element)) {
            for (Element enclosedElement : element.getEnclosedElements()) {
                if (enclosedElement instanceof VariableElement) {
                    addToSupportedTypes((VariableElement) enclosedElement, fieldOption, variableNames);
                }
            }
        }

    }

    private void addMemberVariable(@NotNull FieldAccessor element, @NotNull TypeMirror typeMirror,
                                   @NotNull Map<String, FieldAccessor> variableNames) {
        FieldAccessor previousElement = variableNames.put(element.createGetterCode(), element);
        if (null != previousElement) {
            mMemberVariables.remove(previousElement);
            MessagerUtils.logInfo("Ignoring inherited Member variable with the same variable name in class" +
                                  element.toString() + ", with variable name " + previousElement.asType().toString());
        }
        mMemberVariables.put(element, typeMirror);
    }

    private static void checkModifiers(VariableElement variableElement, Set<Modifier> modifiers) {
        if (!modifiers.contains(Modifier.STATIC)) {
            if (modifiers.contains(Modifier.FINAL)) {
                MessagerUtils.reportError("Unable to access field \"" +
                                          variableElement.getSimpleName().toString() + "\" in class " +
                                          variableElement.getEnclosingElement().asType() +
                                          ", field must not be final.", variableElement);
            } else if (modifiers.contains(Modifier.PRIVATE)) {
                MessagerUtils.reportError("Unable to access field \"" +
                                          variableElement.getSimpleName().toString() + "\" in class " +
                                          variableElement.getEnclosingElement().asType() +
                                          ", field must not be private.", variableElement);
            }
        }
    }

    private void addToSupportedTypes(@NotNull VariableElement element, @NotNull FieldOption fieldOption,
                                     @NotNull Map<String, FieldAccessor> variableNames) {
        if (shouldIncludeField(element, fieldOption)) {
            Set<Modifier> modifiers = element.getModifiers();
            if (!modifiers.contains(Modifier.STATIC) && !modifiers.contains(Modifier.TRANSIENT)) {
                checkModifiers(element, modifiers);
                if (!TypeUtils.isAbstract(element)) {
                    mSupportedTypesModel.checkAndAddExternalAdapter(element);
                }
                DebugLog.log(TAG, "\t\tMember variables - " + element.asType().toString());

                addMemberVariable(new DirectFieldAccessor(element), element.asType(), variableNames);
            }
        }
    }

    private boolean shouldIncludeField(@NotNull Element element, @NotNull FieldOption fieldOption) {
        switch (fieldOption) {
            case NONE:
                return false;
            case SERIALIZED_NAME:
                return element.getAnnotation(SerializedName.class) != null;
            case ALL:
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
    public TypeElement getElement() {
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
    public LinkedHashMap<FieldAccessor, TypeMirror> getMemberVariables() {
        return new LinkedHashMap<>(mMemberVariables);
    }
}
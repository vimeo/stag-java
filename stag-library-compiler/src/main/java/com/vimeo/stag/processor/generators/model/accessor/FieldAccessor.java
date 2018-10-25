package com.vimeo.stag.processor.generators.model.accessor;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.processor.utils.Preconditions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;

/**
 * A class representing a field that must be
 * populated from JSON.
 * <p>
 * Created by restainoa on 5/8/17.
 */
public abstract class FieldAccessor {

    @NotNull
    private final VariableElement mVariableElement;

    public FieldAccessor(@NotNull VariableElement element) {
        mVariableElement = element;
    }

    /**
     * Create the code that reads from the field.
     * In the format:
     * <p>
     * <code>
     * object.{getterCode};
     * </code>
     *
     * @return a not null string that represents
     * code that can be used to access the field.
     */
    @NotNull
    public abstract String createGetterCode();

    /**
     * Create the code that assigns the field
     * to the assigned code. In the format:
     * <p>
     * <code>
     * object.{setterCode};
     * </code>
     *
     * @param assignment the code that this field should
     *                   be assigned to.
     * @return a not null string that represents code
     * that can be used to assign the field.
     */
    @NotNull
    public abstract String createSetterCode(@NotNull String assignment);

    /**
     * The variable name of this field.
     *
     * @return the variable name of this field.
     */
    @NotNull
    protected final String getVariableName() {
        return mVariableElement.getSimpleName().toString();
    }

    /**
     * Determines if this field was marked with
     * a not null annotation that requires the
     * field to be populated.
     *
     * @return true if the field was marked not null,
     * false otherwise.
     */
    public final boolean doesRequireNotNull() {
        for (AnnotationMirror annotationMirror : mVariableElement.getAnnotationMirrors()) {
            switch (annotationMirror.toString()) {
                case "@javax.validation.constraints.NotNull":
                case "@edu.umd.cs.findbugs.annotations.NonNull":
                case "@javax.annotation.Nonnull":
                case "@lombok.NonNull":
                case "@org.eclipse.jdt.annotation.NonNull":
                case "@org.jetbrains.annotations.NotNull":
                case "@android.support.annotation.NonNull":
                    return true;
            }
        }

        return false;
    }

    /**
     * If the field was annotated with the
     * {@link JsonAdapter} annotation, this
     * method will get the class held by that
     * annotation.
     *
     * @return the adapter that the field was annotated with.
     */
    @Nullable
    public final TypeMirror getJsonAdapterType() {
        JsonAdapter annotation = mVariableElement.getAnnotation(JsonAdapter.class);

        if (annotation != null) {
            // Using this trick to get the class type
            // https://blog.retep.org/2009/02/13/getting-class-values-from-annotations-in-an-annotationprocessor/
            try {
                annotation.value();
            } catch (MirroredTypeException mte) {
                return mte.getTypeMirror();
            }
        }

        return null;
    }

    /**
     * If {@link #getJsonAdapterType()} returns
     * a type, this method will determine if that
     * adapter is null safe or not. Otherwise, this
     * method will throw an exception.
     *
     * @return true if the adapter is null safe, false otherwise.
     */
    public final boolean isJsonAdapterNullSafe() {
        JsonAdapter annotation = mVariableElement.getAnnotation(JsonAdapter.class);

        Preconditions.checkNotNull(annotation);

        return annotation.nullSafe();
    }

    /**
     * Returns the accessor in its
     * {@link TypeMirror} form.
     *
     * @return the {@link TypeMirror} associated
     * with this accessor.
     */
    @NotNull
    public final TypeMirror asType() {
        return mVariableElement.asType();
    }

    /**
     * Gets the JSON name for the element the name passed to
     * {@link SerializedName} will be used. If the element is
     * not annotated with {@link SerializedName}, the variable
     * name is used.
     *
     * @return a non null string to use as the JSON key.
     */
    @NotNull
    public final String getJsonName() {

        String name = mVariableElement.getAnnotation(SerializedName.class) != null
                ? mVariableElement.getAnnotation(SerializedName.class).value()
                : null;

        if (name == null || name.isEmpty()) {
            name = mVariableElement.getSimpleName().toString();
        }
        return name;
    }

    /**
     * Returns the alternate names for the {@link Element}.
     *
     * @return an array of alternate names, or null if there are none.
     */
    @Nullable
    public final String[] getAlternateJsonNames() {
        return mVariableElement.getAnnotation(SerializedName.class) != null
                ? mVariableElement.getAnnotation(SerializedName.class).alternate()
                : null;
    }

}

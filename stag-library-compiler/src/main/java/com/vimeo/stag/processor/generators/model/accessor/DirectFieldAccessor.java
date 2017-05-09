package com.vimeo.stag.processor.generators.model.accessor;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.VariableElement;

/**
 * A variation of the {@link FieldAccessor}
 * which populates the field by directly
 * accessing the field.
 * <p>
 * Created by restainoa on 5/8/17.
 */
public class DirectFieldAccessor extends FieldAccessor {

    public DirectFieldAccessor(@NotNull VariableElement element) {
        super(element);
    }

    @NotNull
    @Override
    public String createGetterCode() {
        return getVariableName();
    }

    @NotNull
    @Override
    public String createSetterCode(@NotNull String assignment) {
        return getVariableName() + " = " + assignment;
    }
}

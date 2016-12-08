package com.vimeo.stag.processor.generators;


import com.google.gson.annotations.SerializedName;
import com.squareup.javapoet.TypeSpec;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;

public abstract class AdapterGenerator {

    /**
     * If the element is not annotated with {@link SerializedName}, the variable name is used.
     */
    @NotNull
    protected static String getJsonName(@NotNull Element element) {
        String name = (null != element.getAnnotation(SerializedName.class)) ?
                element.getAnnotation(SerializedName.class).value() : null;

        if (null == name || name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        return name;
    }

    @NotNull
    public abstract TypeSpec getTypeAdapterSpec(@NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                                @NotNull StagGenerator stagGenerator);
}

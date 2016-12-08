package com.vimeo.stag.processor.generators;


import com.squareup.javapoet.TypeSpec;
import com.vimeo.stag.GsonAdapterKey;

import org.jetbrains.annotations.NotNull;

import javax.lang.model.element.Element;

public abstract class AdapterGenerator {

    /**
     * If the element is not annotated with {@link GsonAdapterKey}, the variable name is used.
     */
    @NotNull
    protected static String getJsonName(@NotNull Element element) {
        String name = (null != element.getAnnotation(GsonAdapterKey.class)) ?
                element.getAnnotation(GsonAdapterKey.class).value() : null;

        if (null == name || name.isEmpty()) {
            name = element.getSimpleName().toString();
        }
        return name;
    }

    @NotNull
    public abstract TypeSpec getTypeAdapterSpec(@NotNull TypeTokenConstantsGenerator typeTokenConstantsGenerator,
                                                @NotNull StagGenerator stagGenerator);
}

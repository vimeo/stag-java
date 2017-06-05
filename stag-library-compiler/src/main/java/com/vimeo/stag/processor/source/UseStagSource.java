package com.vimeo.stag.processor.source;


import com.vimeo.stag.UseStag;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.MessagerUtils;

import org.jetbrains.annotations.NotNull;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;

/**
 * Created by restainoa on 6/5/17.
 */

public class UseStagSource {

    @NotNull
    private final RoundEnvironment mRoundEnvironment;

    public UseStagSource(@NotNull RoundEnvironment roundEnvironment) {
        mRoundEnvironment = roundEnvironment;
    }


    @NotNull
    public Flowable<TypeElement> elements() {
        return Flowable.fromIterable(mRoundEnvironment.getElementsAnnotatedWith(UseStag.class))
                .map(toTypeElements())
                .filter(filterSupportedElements());
    }

    @NotNull
    private static Predicate<TypeElement> filterSupportedElements() {
        return ElementUtils::isSupportedElementKind;
    }

    @NotNull
    private static Function<Element, TypeElement> toTypeElements() {
        return element -> {
            if (element instanceof TypeElement) {
                return (TypeElement) element;
            }
            MessagerUtils.reportError("@UseStag annotation can only be used on classes and enums", element);
            return null;
        };
    }

}

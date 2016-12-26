package com.vimeo.stag.processor.generators;


import com.vimeo.stag.UseStag;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

public class ExternalAdapterInfo {

    @NotNull
    public final Element mExternalClassType;

    @NotNull
    public final TypeElement mAdapterType;

    @NotNull
    public final ExecutableElement mAdapterConstructor;

    public ExternalAdapterInfo(Element typeElement, TypeElement adapterTypeElement, ExecutableElement adapterConstructor) {
        this.mExternalClassType = typeElement;
        this.mAdapterType = adapterTypeElement;
        this.mAdapterConstructor = adapterConstructor;
    }

    @Nullable
    public static ExternalAdapterInfo checkAndInitialize(@NotNull Elements elementUtils, @NotNull String stagFactoryGeneratedName, @NotNull VariableElement variableElement) {
        TypeMirror typeMirror = variableElement.asType();
        if(!TypeUtils.isSupportedPrimitive(typeMirror.toString()) && typeMirror instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType)typeMirror;
            Element typeElement = declaredType.asElement();
            UseStag useStag = null != typeElement ? typeElement.getAnnotation(UseStag.class) : null;
            if(null != useStag) {
                TypeElement adapterTypeElement = elementUtils.getTypeElement(typeElement.toString() + "$TypeAdapter");
                if(null != adapterTypeElement) {
                    for (Element adapterEnclosedElement : adapterTypeElement.getEnclosedElements()) {
                        if(adapterEnclosedElement instanceof ExecutableElement) {
                            ExecutableElement executableElement = ((ExecutableElement)adapterEnclosedElement);
                            Name name = executableElement.getSimpleName();
                            if(name.contentEquals("<init>") && executableElement.getParameters().size() >= 2  && !stagFactoryGeneratedName.equals(executableElement.getParameters().get(1).asType().toString())) {
                                return new ExternalAdapterInfo(typeElement, adapterTypeElement,  executableElement);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    @NotNull
    public String getInitializer(@NotNull String gsonVariableName, @NotNull String concatenatedTypeAdapters) {
        int paramsSize = mAdapterConstructor.getParameters().size();
        if(paramsSize == 2) {
            return "new " + mAdapterType.toString() + "(" + gsonVariableName + ", " + getFactoryInitializer() + ")";
        } else {
            return "new " + mAdapterType.toString() + "(" + gsonVariableName + ", " + getFactoryInitializer() + concatenatedTypeAdapters + ")";
        }
    }

    @NotNull
    public String getFactoryInitializer() {
        return "new " + mAdapterConstructor.getParameters().get(1).asType() + "()";
    }
}

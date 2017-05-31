package com.vimeo.stag.processor.generators;


import com.vimeo.stag.UseStag;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public final class ExternalAdapterInfo {

    @NotNull private static final Set<String> sCheckedClasses = new HashSet<>();

    @NotNull private final TypeElement mExternalClassType;
    @NotNull private final TypeElement mAdapterType;
    @NotNull private final ExecutableElement mAdapterConstructor;

    private ExternalAdapterInfo(@NotNull TypeElement typeElement,
                                @NotNull TypeElement adapterTypeElement,
                                @NotNull ExecutableElement adapterConstructor) {
        mExternalClassType = typeElement;
        mAdapterType = adapterTypeElement;
        mAdapterConstructor = adapterConstructor;
    }

    @NotNull
    public Element getExternalClass() {
        return mExternalClassType;
    }

    /**
     * Add adapters for the external models.
     *
     * @param stagFactoryGeneratedName stagFactoryGeneratedName
     * @param typeMirror               typeMirror
     * @param externalAdapterInfoSet   externalAdapterInfoSet
     */
    public static void addExternalAdapters(@NotNull String stagFactoryGeneratedName,
                                           @NotNull TypeMirror typeMirror,
                                           @NotNull Set<ExternalAdapterInfo> externalAdapterInfoSet) {
        if (!TypeUtils.isSupportedPrimitive(typeMirror.toString()) && typeMirror instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) typeMirror;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            UseStag useStag = null != typeElement ? typeElement.getAnnotation(UseStag.class) : null;
            /*
             * Make sure the external model is annotated with @UseStag
             */
            if (useStag != null) {
                ClassInfo classInfo = new ClassInfo(typeElement.asType());
                String classAdapterName =
                        FileGenUtils.unescapeEscapedString(classInfo.getTypeAdapterQualifiedClassName());
                if (!sCheckedClasses.contains(classAdapterName)) {
                    sCheckedClasses.add(classAdapterName);
                    TypeElement adapterTypeElement =
                            ElementUtils.getTypeElementFromQualifiedName(classAdapterName);
                    if (null != adapterTypeElement) {
                        adapterTypeElement.getEnclosedElements()
                                .stream()
                                .filter(adapterEnclosedElement -> adapterEnclosedElement instanceof ExecutableElement)
                                .forEach(adapterEnclosedElement -> {
                                    ExecutableElement executableElement =
                                            ((ExecutableElement) adapterEnclosedElement);
                                    Name name = executableElement.getSimpleName();
                                    if (name.contentEquals("<init>") &&
                                        executableElement.getParameters().size() >= 2 &&
                                        !stagFactoryGeneratedName.equals(
                                                executableElement.getParameters().get(1).asType().toString())) {
                                        ExternalAdapterInfo result =
                                                new ExternalAdapterInfo(typeElement, adapterTypeElement,
                                                                        executableElement);
                                        sCheckedClasses.add(classAdapterName);
                                        externalAdapterInfoSet.add(result);
                                    }
                                });
                    }
                }
            }
            List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            if (null != typeArguments) {
                for (TypeMirror typeArgument : typeArguments) {
                    addExternalAdapters(stagFactoryGeneratedName, typeArgument, externalAdapterInfoSet);
                }
            }
        }
    }

    @NotNull
    String getInitializer(@NotNull String gsonVariableName, @NotNull String concatenatedTypeAdapters) {
        int paramsSize = mAdapterConstructor.getParameters().size();
        if (paramsSize == 2) {
            return "new " + FileGenUtils.escapeStringForCodeBlock(mAdapterType.toString()) + "(" +
                   gsonVariableName + ", " + getFactoryInitializer() + ")";
        } else {
            return "new " + FileGenUtils.escapeStringForCodeBlock(mAdapterType.toString()) + "(" +
                   gsonVariableName + ", " + getFactoryInitializer() + concatenatedTypeAdapters + ")";
        }
    }

    @NotNull
    String getFactoryInitializer() {
        return "new " + mAdapterConstructor.getParameters().get(1).asType() + "()";
    }
}
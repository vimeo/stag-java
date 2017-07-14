package com.vimeo.stag.processor.generators.model.accessor;

import com.vimeo.stag.processor.utils.MessagerUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

/**
 * A variation of the {@link FieldAccessor}
 * which populates the field by accessing
 * it through getters and setters.
 * <p>
 * Created by restainoa on 5/8/17.
 */
public class MethodFieldAccessor extends FieldAccessor {

    /**
     * Field naming notation,
     * used to determine the
     * names of the accessor methods.
     */
    public enum Notation {
        STANDARD,
        HUNGARIAN
    }

    @NotNull private final String mSetterName;
    @NotNull private final String mGetterName;

    public MethodFieldAccessor(@NotNull VariableElement element, @NotNull Notation notation) throws UnsupportedOperationException {
        super(element);

        mSetterName = findSetterMethodName(element, notation);
        mGetterName = findGetterMethodName(element, notation);
    }

    @NotNull
    @Override
    public String createGetterCode() {
        return mGetterName + "()";
    }

    @NotNull
    @Override
    public String createSetterCode(@NotNull String assignment) {
        return mSetterName + '(' + assignment + ')';
    }

    @NotNull
    private static List<ExecutableElement> getSiblingMethods(@NotNull VariableElement variableElement) {
        List<ExecutableElement> methodElements = new ArrayList<>();
        List<? extends Element> otherElements = variableElement.getEnclosingElement().getEnclosedElements();

        for (Element element : otherElements) {
            if (element.getKind() == ElementKind.METHOD && element instanceof ExecutableElement) {
                methodElements.add((ExecutableElement) element);
            }
        }

        return methodElements;
    }

    @NotNull
    private static String findSetterMethodName(@NotNull VariableElement variableElement,
                                               @NotNull Notation namingNotation) throws UnsupportedOperationException {
        MessagerUtils.logInfo("Looking for setter");

        for (ExecutableElement method : getSiblingMethods(variableElement)) {

            List<? extends VariableElement> parameters = method.getParameters();

            if (method.getReturnType().getKind() == TypeKind.VOID &&
                parameters.size() == 1 &&
                TypeUtils.areEqual(parameters.get(0).asType(), variableElement.asType()) &&
                method.getSimpleName().toString().equals("set" + getVariableNameAsMethodName(variableElement, namingNotation))) {
                MessagerUtils.logInfo("Found setter");

                return method.getSimpleName().toString();
            }

        }

        throw new UnsupportedOperationException("Unable to find setter for variable");
    }

    @NotNull
    private static String findGetterMethodName(@NotNull VariableElement variableElement,
                                               @NotNull Notation namingNotation) throws UnsupportedOperationException {
        MessagerUtils.logInfo("Looking for getter");

        for (ExecutableElement method : getSiblingMethods(variableElement)) {

            if (TypeUtils.areEqual(method.getReturnType(), variableElement.asType()) &&
                method.getParameters().isEmpty() &&
                method.getSimpleName().toString().equals("get" + getVariableNameAsMethodName(variableElement, namingNotation))) {

                MessagerUtils.logInfo("Found getter");

                return method.getSimpleName().toString();
            }

        }

        throw new UnsupportedOperationException("Unable to find getter for variable");
    }

    @NotNull
    private static String getVariableNameAsMethodName(@NotNull VariableElement variableElement,
                                                      @NotNull Notation notation) {
        String variableName = variableElement.getSimpleName().toString();

        switch (notation) {
            case STANDARD:
                return Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1);
            case HUNGARIAN:
                return variableName.substring(1, variableName.length());
            default:
                throw new UnsupportedOperationException("Unknown notation type");
        }

    }

}

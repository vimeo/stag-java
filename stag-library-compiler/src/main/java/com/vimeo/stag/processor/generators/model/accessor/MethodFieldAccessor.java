package com.vimeo.stag.processor.generators.model.accessor;

import com.vimeo.stag.processor.utils.MessagerUtils;

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

    @NotNull private final String mSetterName;
    @NotNull private final String mGetterName;

    public MethodFieldAccessor(@NotNull VariableElement element) throws UnsupportedOperationException {
        super(element);

        mSetterName = findSetterMethodName(element);
        mGetterName = findGetterMethodName(element);
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

        MessagerUtils.logInfo("Looking for setter and getter");

        for (Element element : otherElements) {
            if (element.getKind() == ElementKind.METHOD && element instanceof ExecutableElement) {
                methodElements.add((ExecutableElement) element);
            }
        }

        return methodElements;
    }

    @NotNull
    private static String findSetterMethodName(@NotNull VariableElement variableElement) throws UnsupportedOperationException {
        MessagerUtils.logInfo("Looking for setter and getter");

        for (ExecutableElement method : getSiblingMethods(variableElement)) {

            List<? extends VariableElement> parameters = method.getParameters();

            if (method.getReturnType().getKind() == TypeKind.VOID &&
                parameters.size() == 1 &&
                parameters.get(0).asType().equals(variableElement.asType()) &&
                method.getSimpleName().toString().equals("set" + getVariableNameAsMethodName(variableElement))) {
                MessagerUtils.logInfo("Found setter");

                return method.getSimpleName().toString();
            }

        }

        throw new UnsupportedOperationException("Unable to find setter for variable");
    }

    @NotNull
    private static String findGetterMethodName(@NotNull VariableElement variableElement) throws UnsupportedOperationException {
        MessagerUtils.logInfo("Looking for setter and getter");

        for (ExecutableElement method : getSiblingMethods(variableElement)) {

            if (method.getReturnType().equals(variableElement.asType()) &&
                method.getParameters().isEmpty() &&
                method.getSimpleName().toString().equals("get" + getVariableNameAsMethodName(variableElement))) {

                MessagerUtils.logInfo("Found getter");

                return method.getSimpleName().toString();
            }

        }

        throw new UnsupportedOperationException("Unable to find getter for variable");
    }

    @NotNull
    private static String getVariableNameAsMethodName(@NotNull VariableElement variableElement) {
        // if hungarian notation, need different logic
        String variableName = variableElement.getSimpleName().toString();

        return Character.toUpperCase(variableName.charAt(0)) + variableName.substring(1, variableName.length());
    }

}

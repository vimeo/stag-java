package com.vimeo.stag.processor;

import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.processor.generators.ParseGenerator;
import com.vimeo.stag.processor.generators.StagGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
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
@SuppressWarnings("StringConcatenationMissingWhitespace")
@SupportedAnnotationTypes("com.vimeo.stag.GsonAdapterKey")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public final class StagProcessor extends AbstractProcessor {

    private static final boolean DEBUG = true;

    private boolean mHasBeenProcessed;
    private final Set<String> mSupportedTypes = new HashSet<>();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(GsonAdapterKey.class.getCanonicalName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_7;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (mHasBeenProcessed) {
            return true;
        }
        log("Beginning @GsonAdapterKey annotation processing");
        mHasBeenProcessed = true;
        Map<TypeMirror, List<VariableElement>> variableMap = new HashMap<>();
        for (Element element : roundEnv.getElementsAnnotatedWith(GsonAdapterKey.class)) {
            if (element instanceof VariableElement) {
                final VariableElement variableElement = (VariableElement) element;

                Set<Modifier> modifiers = variableElement.getModifiers();
                if (modifiers.contains(Modifier.FINAL)) {
                    throw new RuntimeException("Unable to access field \"" +
                                               variableElement.getSimpleName().toString() + "\" in class " +
                                               variableElement.getEnclosingElement().asType() +
                                               ", field must not be final.");
                } else if (!modifiers.contains(Modifier.PUBLIC)) {
                    throw new RuntimeException("Unable to access field \"" +
                                               variableElement.getSimpleName().toString() + "\" in class " +
                                               variableElement.getEnclosingElement().asType() +
                                               ", field must public.");
                }

                TypeMirror enclosingClass = variableElement.getEnclosingElement().asType();

                if (isParameterizedType(enclosingClass) && isParameterizedTypeGeneric(enclosingClass)) {
                    throw new RuntimeException("Generic parameterized classes are currently unsupported");
                }

                mSupportedTypes.add(enclosingClass.toString());
                addToListMap(variableMap, enclosingClass, variableElement);
            }
        }

        try {
            ParseGenerator parseGenerator =
                    new ParseGenerator(mSupportedTypes, processingEnv.getFiler(), variableMap);
            parseGenerator.generateParsingCode();
            StagGenerator adapterGenerator =
                    new StagGenerator(processingEnv.getFiler(), variableMap.keySet());
            adapterGenerator.generateTypeAdapters();
        } catch (IOException e) {
            logError("Error while processing annotations");
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
            return true;
        }
        log("Successfully processed @GsonAdapterKey annotations");
        return true;
    }

    private static void debugPrintTypes(TypeMirror type) {
        if (type instanceof DeclaredType) { // e.g. ArrayList<E>
            log("declared type: " + ((DeclaredType) type).asElement().getSimpleName());  // List

            for (TypeMirror arg : ((DeclaredType) type).getTypeArguments()) { // E
                debugPrintTypes(arg);
            }
        } else {
            log("unknown type: " + type.toString());
        }
    }

    private static boolean isParameterizedType(TypeMirror type) {
        return !((DeclaredType) type).getTypeArguments().isEmpty();
    }

    private static boolean isParameterizedTypeGeneric(TypeMirror type) {
        TypeMirror typeMirror = ((DeclaredType) type).getTypeArguments().get(0);
        log(typeMirror.toString());
        return typeMirror.getKind() == TypeKind.TYPEVAR;
    }

    private static void addToListMap(Map<TypeMirror, List<VariableElement>> map, TypeMirror key,
                                     VariableElement value) {
        if (key == null || value == null) {
            return;
        }
        List<VariableElement> list = map.get(key);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(value);
        map.put(key, list);
    }

    private static void log(CharSequence message) {
        if (DEBUG) {
            //noinspection UseOfSystemOutOrSystemErr
            System.out.println(message);
        }
    }

    private void logError(CharSequence message) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, message);
    }

}

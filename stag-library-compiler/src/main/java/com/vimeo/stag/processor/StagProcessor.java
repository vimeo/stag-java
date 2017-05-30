/*
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
package com.vimeo.stag.processor;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import com.vimeo.stag.UseStag;
import com.vimeo.stag.processor.generators.AdapterGenerator;
import com.vimeo.stag.processor.generators.EnumTypeAdapterGenerator;
import com.vimeo.stag.processor.generators.ExternalAdapterInfo;
import com.vimeo.stag.processor.generators.StagGenerator;
import com.vimeo.stag.processor.generators.TypeAdapterGenerator;
import com.vimeo.stag.processor.generators.model.AnnotatedClass;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.generators.model.accessor.MethodFieldAccessor.Notation;
import com.vimeo.stag.processor.utils.DebugLog;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.KnownTypeAdapterFactoriesUtils;
import com.vimeo.stag.processor.utils.MessagerUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@AutoService(Processor.class)
@SupportedAnnotationTypes(value = {"com.vimeo.stag.UseStag"})
@SupportedOptions(value = {StagProcessor.OPTION_PACKAGE_NAME, StagProcessor.OPTION_DEBUG, StagProcessor.OPTION_HUNGARIAN_NOTATION})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public final class StagProcessor extends AbstractProcessor {

    public static volatile boolean DEBUG;
    static final String OPTION_DEBUG = "stagDebug";
    static final String OPTION_PACKAGE_NAME = "stagGeneratedPackageName";
    static final String OPTION_HUNGARIAN_NOTATION = "stagAssumeHungarianNotation";
    private static final String DEFAULT_GENERATED_PACKAGE_NAME = "com.vimeo.stag.generated";
    private boolean mHasBeenProcessed;

    @Override
    public SourceVersion getSupportedSourceVersion() {
        // Always try to support the latest Java version
        return SourceVersion.latestSupported();
    }

    private static boolean getDebugBoolean(@NotNull ProcessingEnvironment processingEnvironment) {
        String debugString = processingEnvironment.getOptions().get(OPTION_DEBUG);
        if (debugString != null) {
            return Boolean.valueOf(debugString);
        }
        return false;
    }

    private static boolean getAssumeHungarianNotation(@NotNull ProcessingEnvironment processingEnvironment) {
        String debugString = processingEnvironment.getOptions().get(OPTION_HUNGARIAN_NOTATION);
        if (debugString != null) {
            return Boolean.valueOf(debugString);
        }
        return false;
    }

    @NotNull
    private static String getOptionalPackageName(@NotNull ProcessingEnvironment processingEnvironment) {
        String packageName = processingEnvironment.getOptions().get(OPTION_PACKAGE_NAME);
        if (packageName == null || packageName.isEmpty()) {
            packageName = DEFAULT_GENERATED_PACKAGE_NAME;
        }
        return packageName;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (mHasBeenProcessed) {
            return true;
        }

        mHasBeenProcessed = true;

        DEBUG = getDebugBoolean(processingEnv);

        String packageName = getOptionalPackageName(processingEnv);

        boolean assumeHungarianNotation = getAssumeHungarianNotation(processingEnv);

        TypeUtils.initialize(processingEnv.getTypeUtils());
        ElementUtils.initialize(processingEnv.getElementUtils());
        MessagerUtils.initialize(processingEnv.getMessager());

        String stagFactoryGeneratedName = StagGenerator.getGeneratedFactoryClassAndPackage(packageName);

        Notation notation = assumeHungarianNotation ? Notation.HUNGARIAN : Notation.STANDARD;

        SupportedTypesModel supportedTypesModel = new SupportedTypesModel(stagFactoryGeneratedName, notation);

        DebugLog.log("\nBeginning @UseStag annotation processing\n");

        // Pick up the classes annotated with @UseStag
        Set<? extends Element> useStagElements = roundEnv.getElementsAnnotatedWith(UseStag.class);
        for (Element useStagElement : useStagElements) {
            processSupportedElements(supportedTypesModel, useStagElement);
        }

        try {
            Set<TypeMirror> supportedTypes = AnnotatedClass.annotatedClassToTypeMirror(supportedTypesModel.getSupportedTypes());
            try {
                supportedTypes.addAll(KnownTypeAdapterFactoriesUtils.loadKnownTypes(processingEnv, packageName));
            } catch (Exception ignored) {
            }

            Set<ExternalAdapterInfo> externalAdapterInfoSet = supportedTypesModel.getExternalSupportedAdapters();

            StagGenerator stagFactoryGenerator = new StagGenerator(packageName, supportedTypes, externalAdapterInfoSet, supportedTypesModel);

            for (AnnotatedClass annotatedClass : supportedTypesModel.getSupportedTypes()) {
                TypeElement element = annotatedClass.getElement();
                if ((TypeUtils.isConcreteType(element) || TypeUtils.isParameterizedType(element)) &&
                    !TypeUtils.isAbstract(element)) {
                    generateTypeAdapter(supportedTypesModel, element, stagFactoryGenerator);
                }
            }

            generateStagFactory(stagFactoryGenerator, packageName);
            KnownTypeAdapterFactoriesUtils.writeKnownTypes(processingEnv, packageName, supportedTypes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DebugLog.log("\nSuccessfully processed @UseStag annotations\n");

        return true;
    }

    private void generateStagFactory(@NotNull StagGenerator stagGenerator,
                                     @NotNull String packageName) throws IOException {
        // Create the type spec
        TypeSpec typeSpec = stagGenerator.createStagSpec();

        // Write the type spec to a file
        writeTypeSpecToFile(typeSpec, packageName);
    }

    private void generateTypeAdapter(@NotNull SupportedTypesModel supportedTypesModel,
                                     @NotNull TypeElement element,
                                     @NotNull StagGenerator stagGenerator) throws IOException {

        ClassInfo classInfo = new ClassInfo(element.asType());

        AdapterGenerator independentAdapter = element.getKind() == ElementKind.ENUM ?
            new EnumTypeAdapterGenerator(classInfo, element) :
            new TypeAdapterGenerator(supportedTypesModel, classInfo);

        // Create the type spec
        TypeSpec typeAdapterSpec = independentAdapter.createTypeAdapterSpec(stagGenerator);

        // Write the type spec to a file
        writeTypeSpecToFile(typeAdapterSpec, classInfo.getPackageName());
    }

    private void writeTypeSpecToFile(@NotNull TypeSpec typeSpec, @NotNull String packageName) throws IOException {

        // Create the Java file
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();

        Filer filer = processingEnv.getFiler();

        // Write the Java file to disk
        FileGenUtils.writeToFile(javaFile, filer);
    }

    /**
     * Adds all classes annotated with {@link UseStag}
     * to the supported type model. It does this recursively
     * for unsupported types. Supported types handle their
     * own enclosed element adding. Unsupported types that
     * could be annotated are @interface and interface. Enums
     * and classes are supported.
     *
     * @param supportedTypesModel the supported types model
     * @param useStagElement      the element to add to the
     *                            supported type model.
     */
    private static void processSupportedElements(@NotNull SupportedTypesModel supportedTypesModel,
                                                 @NotNull Element useStagElement) {
        if (ElementUtils.isSupportedElementKind(useStagElement)) {
            TypeMirror rootType = useStagElement.asType();
            DebugLog.log("Annotated type: " + rootType + "\n");
            supportedTypesModel.addSupportedType(rootType);
        }

        List<? extends Element> enclosedElements = useStagElement.getEnclosedElements();
        for (Element element : enclosedElements) {
            processSupportedElements(supportedTypesModel, element);
        }
    }
}
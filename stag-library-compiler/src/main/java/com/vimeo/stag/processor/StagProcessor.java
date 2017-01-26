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
import com.vimeo.stag.UseStag;
import com.vimeo.stag.processor.generators.AdapterGenerator;
import com.vimeo.stag.processor.generators.EnumTypeAdapterGenerator;
import com.vimeo.stag.processor.generators.StagGenerator;
import com.vimeo.stag.processor.generators.TypeAdapterGenerator;
import com.vimeo.stag.processor.generators.TypeTokenConstantsGenerator;
import com.vimeo.stag.processor.generators.model.ClassInfo;
import com.vimeo.stag.processor.generators.model.SupportedTypesModel;
import com.vimeo.stag.processor.utils.DebugLog;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.FileGenUtils;
import com.vimeo.stag.processor.utils.KnownTypeAdapterFactoriesUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
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
@SupportedAnnotationTypes(value = {"com.vimeo.stag.UseStag", "com.vimeo.stag.GsonAdapterKey"})
@SupportedOptions(value = {"stagGeneratedPackageName"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public final class StagProcessor extends AbstractProcessor {

    public static final boolean DEBUG = false;
    private static final String OPTION_PACKAGE_NAME = "stagGeneratedPackageName";
    private static final String DEFAULT_GENERATED_PACKAGE_NAME = "com.vimeo.stag.generated";
    private boolean mHasBeenProcessed;

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(UseStag.class.getCanonicalName());
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

        String packageName = processingEnv.getOptions().get(OPTION_PACKAGE_NAME);
        if (packageName == null || packageName.isEmpty()) {
            packageName = DEFAULT_GENERATED_PACKAGE_NAME;
        }

        String stagFactoryGeneratedName = StagGenerator.getGeneratedFactoryClassAndPackage(packageName);
        TypeUtils.initialize(processingEnv.getTypeUtils());
        ElementUtils.initialize(processingEnv.getElementUtils());
        SupportedTypesModel.getInstance().initialize(stagFactoryGeneratedName);

        DebugLog.log("\nBeginning @UseStag annotation processing\n");

        mHasBeenProcessed = true;

        Set<? extends Element> rootElements = roundEnv.getRootElements();
        for (Element rootElement : rootElements) {
            if (rootElement.getAnnotation(UseStag.class) != null) {
                SupportedTypesModel.getInstance().getSupportedType(rootElement.asType());
            }
        }

        Filer filer = processingEnv.getFiler();
        try {
            Set<TypeMirror> mSupportedTypes = SupportedTypesModel.getInstance().getSupportedTypesMirror();
            try {
                mSupportedTypes.addAll(
                        KnownTypeAdapterFactoriesUtils.loadKnownTypes(processingEnv, packageName));
            } catch (Exception ignored) {
            }

            StagGenerator adapterGenerator = new StagGenerator(packageName, filer, mSupportedTypes,
                                                               SupportedTypesModel.getInstance()
                                                                       .getExternalSupportedAdapters());
            TypeTokenConstantsGenerator typeTokenConstantsGenerator =
                    new TypeTokenConstantsGenerator(filer, packageName);

            Set<Element> list = SupportedTypesModel.getInstance().getSupportedElements();
            for (Element element : list) {
                if ((TypeUtils.isConcreteType(element) || TypeUtils.isParameterizedType(element)) &&
                    !TypeUtils.isAbstract(element)) {
                    ClassInfo classInfo = new ClassInfo(element.asType());
                    AdapterGenerator independentAdapter =
                            element.getKind() == ElementKind.ENUM ? new EnumTypeAdapterGenerator(classInfo,
                                                                                                 element) : new TypeAdapterGenerator(
                                    classInfo);
                    JavaFile javaFile = JavaFile.builder(classInfo.getPackageName(),
                                                         independentAdapter.getTypeAdapterSpec(
                                                                 typeTokenConstantsGenerator,
                                                                 adapterGenerator)).build();
                    FileGenUtils.writeToFile(javaFile, filer);
                }
            }

            adapterGenerator.generateTypeAdapterFactory(packageName);
            typeTokenConstantsGenerator.generateTypeTokenConstants();
            KnownTypeAdapterFactoriesUtils.writeKnownTypes(processingEnv, packageName, mSupportedTypes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DebugLog.log("\nSuccessfully processed @UseStag annotations\n");

        return true;
    }
}
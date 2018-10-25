package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

public class NativeJavaModelTest {

    @Test
    public void typeAdapterWasGenerated_NativeJavaModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(NativeJavaModel.class);
    }

    @Test
    public void typeAdapterWasGenerated_NativeJavaModel_Nested() throws Exception {
        Utils.verifyTypeAdapterGeneration(NativeJavaModel.Nested.class);
    }

    @Test
    public void typeNoAdapterWasNotGenerated_NativeJavaModel_NestedWithoutAnnotation() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(NativeJavaModel.NestedWithoutAnnotation.class);
    }

    @Test
    public void typeAdapterWasGenerated_NativeJavaModel_NestedExtension() throws Exception {
        Utils.verifyTypeAdapterGeneration(NativeJavaModel.NestedExtension.class);
    }

    @Test
    public void typeAdapterWasNotGenerated_NativeJavaModel_NestedExtensionWithoutAnnotation() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(NativeJavaModel.NestedExtensionWithoutAnnotation.class);
    }

    @Test
    public void typeAdapterWasGenerated_NativeJavaModel_NestedExtensionFromNoAnnotation() throws Exception {
        Utils.verifyTypeAdapterGeneration(NativeJavaModel.NestedExtensionFromNoAnnotation.class);
    }

}

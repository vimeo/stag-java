package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

public class NativeJavaModelExtensionTest {

    @Test
    public void typeAdapterWasGenerated_NativeJavaModelExtension() throws Exception {
        Utils.verifyTypeAdapterGeneration(NativeJavaModelExtension.class);
    }

}

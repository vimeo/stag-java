package com.vimeo.integration_test_android;

import org.junit.Test;

import verification.Utils;

public class ComplexGenericClassExtendedTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ComplexGenericClassExtended.class);
    }

}

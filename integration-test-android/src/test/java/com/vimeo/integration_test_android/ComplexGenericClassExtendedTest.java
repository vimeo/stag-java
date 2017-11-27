package com.vimeo.integration_test_android;

import org.junit.Test;

public class ComplexGenericClassExtendedTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ComplexGenericClassExtended.class);
    }

}
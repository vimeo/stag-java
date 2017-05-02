package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

public class ComplexGenericClassExtendedTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ComplexGenericClassExtended.class);
    }

}
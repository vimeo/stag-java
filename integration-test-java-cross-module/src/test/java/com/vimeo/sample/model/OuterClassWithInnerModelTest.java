package com.vimeo.sample.model;

import verification.Utils;

import org.junit.Test;

public class OuterClassWithInnerModelTest {

    @Test
    public void innerTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(OuterClassWithInnerModel.InnerModel.class);
    }

    @Test
    public void outerTypeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(OuterClassWithInnerModel.class);
    }

}

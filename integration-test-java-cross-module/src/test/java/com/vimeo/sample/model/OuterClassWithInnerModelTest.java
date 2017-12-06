package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

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

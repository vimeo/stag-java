package com.vimeo.sample;

import com.vimeo.sample.model.OuterClassWithInnerModel;

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

package com.vimeo.sample.model.root;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.DynamicallyTypedModel;
import com.vimeo.sample.model.DynamicallyTypedWildcard;

import org.junit.Test;

public class DynamicallyTypedModelTest {

    @Test
    public void verifyTypeAdapterWasGenerated_DynamicallyTypedWildcard() throws Exception {
        Utils.verifyTypeAdapterGeneration(DynamicallyTypedWildcard.class);
    }

    @Test
    public void verifyTypeAdapterWasGenerated_DynamicallyTypedModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(DynamicallyTypedModel.class);
    }
}
package com.vimeo.sample_java_model;

import org.junit.Test;

/**
 * Integration tests for {@link DynamicallyTypedModel}.
 */
public class DynamicallyTypedModelTest {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(DynamicallyTypedModel.class);
    }
}

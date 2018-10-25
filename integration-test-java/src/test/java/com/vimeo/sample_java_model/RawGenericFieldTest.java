package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Integration tests for {@link RawGenericField}.
 */
public class RawGenericFieldTest {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(RawGenericField.class);
    }
}

package com.vimeo.sample.model1;

import verification.Utils;

import org.junit.Test;

/**
 * Unit tests for {@link ParameterizedData}.
 */
public class ParameterizedDataTest {

    @Test
    public void verifyTypeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(ParameterizedData.class);
    }
}

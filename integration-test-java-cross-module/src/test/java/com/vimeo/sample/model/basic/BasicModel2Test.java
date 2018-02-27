package com.vimeo.sample.model.basic;

import verification.Utils;

import org.junit.Test;

/**
 * Unit tests for {@link BasicModel2}.
 */
public class BasicModel2Test {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(BasicModel2.class);
    }

}

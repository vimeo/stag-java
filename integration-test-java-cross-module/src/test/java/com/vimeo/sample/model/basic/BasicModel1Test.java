package com.vimeo.sample.model.basic;

import verification.Utils;

import org.junit.Test;

/**
 * Unit tests for {@link BasicModel1}.
 */
public class BasicModel1Test {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(BasicModel1.class);
    }

}

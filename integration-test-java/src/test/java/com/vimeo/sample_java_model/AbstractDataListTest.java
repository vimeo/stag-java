package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Integration tests for {@link AbstractDataList}.
 */
public class AbstractDataListTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(AbstractDataList.class);
    }

}

package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Integration tests for {@link SuperAbstractDataList}.
 */
public class SuperAbstractDataListTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(SuperAbstractDataList.class);
    }

}

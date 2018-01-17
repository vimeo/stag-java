package com.vimeo.sample_java_model;

import org.junit.Test;

/**
 * Integration tests for {@link SuperAbstractDataList}.
 */
public class SuperAbstractDataListTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(SuperAbstractDataList.class);
    }

}

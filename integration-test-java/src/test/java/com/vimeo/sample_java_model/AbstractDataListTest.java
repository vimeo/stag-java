package com.vimeo.sample_java_model;

import org.junit.Test;

/**
 * Integration tests for {@link AbstractDataList}.
 */
public class AbstractDataListTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(AbstractDataList.class);
    }

}

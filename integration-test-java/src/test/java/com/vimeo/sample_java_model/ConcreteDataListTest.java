package com.vimeo.sample_java_model;

import org.junit.Test;

/**
 * Integration tests for {@link ConcreteDataList}.
 */
public class ConcreteDataListTest {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ConcreteDataList.class);
    }
}

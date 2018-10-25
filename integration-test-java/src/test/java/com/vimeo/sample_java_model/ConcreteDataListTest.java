package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Integration tests for {@link ConcreteDataList}.
 */
public class ConcreteDataListTest {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ConcreteDataList.class);
    }

    @Test
    public void verifyTypeAdapterCorrectness() {
        Utils.verifyTypeAdapterCorrectness(ConcreteDataList.class);
    }
}

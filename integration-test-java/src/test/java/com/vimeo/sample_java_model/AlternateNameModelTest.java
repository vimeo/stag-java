package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Created by restainoa on 2/2/17.
 */
public class AlternateNameModelTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(AlternateNameModel.class);
    }

    @Test
    public void verifyTypeAdapterCorrectness() {
        Utils.verifyTypeAdapterCorrectness(AlternateNameModel.class);
    }
}

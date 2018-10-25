package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Created by restainoa on 2/2/17.
 */
public class AlternateNameModel1Test {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(AlternateNameModel1.class);
    }

    @Test
    public void verifyTypeAdapterCorrectness() {
        Utils.verifyTypeAdapterCorrectness(AlternateNameModel1.class);
    }
}

package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Created by anthonycr on 2/7/17.
 */
public class BaseExternalModelTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(BaseExternalModel.class);
    }

    @Test
    public void verifyTypeAdapterCorrectness() {
        Utils.verifyTypeAdapterCorrectness(BaseExternalModel.class);
    }
}

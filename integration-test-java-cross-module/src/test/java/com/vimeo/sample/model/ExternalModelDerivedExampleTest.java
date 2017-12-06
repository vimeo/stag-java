package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample_java_model.BaseExternalModel;

import org.junit.Test;

/**
 * Created by anthonycr on 2/7/17.
 */
public class ExternalModelDerivedExampleTest {

    @Test
    public void verifyTypeAdapterWasGenerated_ExternalModelDerivedExample() throws Exception {
        Utils.verifyTypeAdapterGeneration(ExternalModelDerivedExample.class);
    }

    @Test
    public void verifyNoTypeAdapterWasGenerated_BaseExternalModel() throws Exception {
        // Since ExternalModelDerivedExample inherits from BaseExternalModel in another
        // module, we need to make sure that Stag doesn't generate an adapter in both modules.
        Utils.verifyNoTypeAdapterGeneration(BaseExternalModel.class);
    }

}
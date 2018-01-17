package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

import org.junit.Test;

@UseStag
public class WildcardModelTest {

    @Test
    public void typeAdapterWasGenerated_WildcardModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(WildcardModel.class);
    }
}
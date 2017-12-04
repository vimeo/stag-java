package com.vimeo.sample_java_model;

import org.junit.Test;

/**
 * Created by anthonycr on 4/9/17.
 */
public class EnumWithFieldsModelTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(EnumWithFieldsModel.class);
    }

}
package com.vimeo.sample_java_model;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ExternalModelGenericTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ExternalModelGeneric.class);
    }

}
package com.vimeo.sample.model;

import verification.Utils;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class IdenticalFieldTypesTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(IdenticalFieldTypes.class);
    }

}

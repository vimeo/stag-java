package com.vimeo.integration_test_android;

import org.junit.Test;

import verification.Utils;

/**
 * Created by restainoa on 2/2/17.
 */
public class ClassWithMapTypesTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ClassWithMapTypes.class);
    }

}

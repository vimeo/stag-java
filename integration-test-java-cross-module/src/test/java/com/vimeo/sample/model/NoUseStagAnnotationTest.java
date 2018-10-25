package com.vimeo.sample.model;

import verification.Utils;

import org.junit.Test;

/**
 * Created by restainoa on 1/30/17.
 */
public class NoUseStagAnnotationTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(NoUseStagAnnotation.class);
    }

}

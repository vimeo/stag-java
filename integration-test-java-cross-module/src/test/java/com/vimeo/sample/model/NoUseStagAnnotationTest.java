package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

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

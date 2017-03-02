package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.NoUseStagAnnotation;

import org.junit.Test;

/**
 * Created by restainoa on 1/30/17.
 */
public class NoUseStagAnnotationTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(NoUseStagAnnotation.class);
    }

}

package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ExternalModelExample2Test {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ExternalModelExample2.class);
    }

}
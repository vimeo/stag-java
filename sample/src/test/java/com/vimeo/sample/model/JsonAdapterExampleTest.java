package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

/**
 * Created by anshul.garg on 03/03/17.
 */
public class JsonAdapterExampleTest {
    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(JsonAdapterExample.class);
    }
}
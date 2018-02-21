package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

/**
 * Created by anshul.garg on 12/04/17.
 */
public class ObjectExampleTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ObjectExample.class);
    }

}

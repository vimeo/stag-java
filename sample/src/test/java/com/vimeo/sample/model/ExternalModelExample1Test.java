package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.external.ExternalModelExample1;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ExternalModelExample1Test {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ExternalModelExample1.class);
    }

}
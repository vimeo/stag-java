package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.external.ExternalModelExample;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ExternalModelExampleTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ExternalModelExample.class);
    }

}
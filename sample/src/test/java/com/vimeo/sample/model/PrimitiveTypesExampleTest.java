package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.scenarios.PrimitiveTypesExample;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class PrimitiveTypesExampleTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(PrimitiveTypesExample.class);
    }

}
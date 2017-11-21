package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.enumeration.EnumExample;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class EnumExampleTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(EnumExample.class);
    }

}

package com.vimeo.sample_model;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ExternalAbstractClassTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(ExternalAbstractClass.class);
    }

}
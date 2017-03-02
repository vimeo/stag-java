package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ClassWithMapTypesTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(ClassWithMapTypes.class);
    }

}
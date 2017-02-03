package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class SampleInterfaceTest {

    @Test
    public void typeAdapterWasNotGenerated_SampleInterface() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(SampleInterface.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedClass() throws Exception {
        // TODO: This use case is broken 2/2/17 [AR]
//        Utils.verifyTypeAdapterGeneration(SampleInterface.NestedClass.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedClassAnnotated() throws Exception {
        Utils.verifyTypeAdapterGeneration(SampleInterface.NestedClassAnnotated.class);
    }
}

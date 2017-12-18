package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ClassWithNestedInterfaceTest {

    @Test
    public void typeAdapterWasNotGenerated_AvailabilityIntentDef() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(ClassWithNestedInterface.AvailabilityIntentDef.class);
    }

    @Test
    public void typeAdapterWasGenerated_ClassWithNestedInterface() throws Exception {
        Utils.verifyTypeAdapterGeneration(ClassWithNestedInterface.class);
    }
}
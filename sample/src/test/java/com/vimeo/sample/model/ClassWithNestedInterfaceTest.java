package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.ClassWithNestedInterface.AvailabilityIntentDef;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ClassWithNestedInterfaceTest {

    @Test
    public void typeAdapterWasNotGenerated_AvailabilityIntentDef() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(AvailabilityIntentDef.class);
    }

    @Test
    public void typeAdapterWasGenerated_ClassWithNestedInterface() throws Exception {
        Utils.verifyTypeAdapterGeneration(ClassWithNestedInterface.class);
    }

}

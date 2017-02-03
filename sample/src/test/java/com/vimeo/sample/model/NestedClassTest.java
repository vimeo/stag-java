package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.NestedClass.NestedWithAnnotation.NestedWithoutAnnotation;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class NestedClassTest {

    @Test
    public void typeAdapterWasGenerated_NestedClass() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedClass.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedClass_Nested() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedClass.Nested.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedClass_NestedWithAnnotation() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedClass.NestedWithAnnotation.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedClass_NestedWithAnnotation_NestedWithoutAnnotation()
            throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedWithoutAnnotation.class);
    }
    
}

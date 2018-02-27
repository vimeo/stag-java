package com.vimeo.sample.model;

import verification.Utils;

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
    public void typeAdapterWasNotGenerated_NestedClass_NestedExtension() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(NestedClass.NestedExtension.class);
    }

    @Test
    public void typeAdapterWasNotGenerated_NestedClass_Nested() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(NestedClass.Nested.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedClass_NestedWithAnnotation() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedClass.NestedWithAnnotation.class);
    }

    @Test
    public void typeAdapterWasNotGenerated_NestedClass_NestedWithAnnotation_NestedWithoutAnnotation()
            throws Exception {
        Utils.verifyNoTypeAdapterGeneration(NestedClass.NestedWithAnnotation.NestedWithoutAnnotation.class);
    }

}

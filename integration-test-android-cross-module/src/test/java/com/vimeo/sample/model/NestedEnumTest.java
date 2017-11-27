package com.vimeo.sample.model;

import com.vimeo.sample.Utils;

import org.junit.Test;

public class NestedEnumTest {

    @Test
    public void typeAdapterWasGenerated_NestedEnum() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedEnum.class);
    }

    @Test
    public void typeAdapterWasNotGenerated_NestedEnum_Nested() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(NestedEnum.Nested.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedEnum_NestedWithAnnotation() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedEnum.NestedWithAnnotation.class);
    }

}
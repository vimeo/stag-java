package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Unit tests for {@link NativeArrayTypes}.
 */
public class NativeArrayTypesTest {

    @Test
    public void verifyTypeAdapterWasGenerated() {
        Utils.verifyTypeAdapterGeneration(NativeArrayTypes.class);
    }
}

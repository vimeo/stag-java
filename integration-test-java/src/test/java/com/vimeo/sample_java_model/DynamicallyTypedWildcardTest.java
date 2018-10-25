package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Integration tests for {@link DynamicallyTypedWildcard}.
 */
public class DynamicallyTypedWildcardTest {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(DynamicallyTypedWildcard.class);
    }
}

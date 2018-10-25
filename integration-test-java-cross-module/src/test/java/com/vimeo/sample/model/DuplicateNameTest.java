package com.vimeo.sample.model;

import verification.Utils;

import org.junit.Test;

/**
 * Unit tests for {@link DuplicateName}.
 */
public class DuplicateNameTest {

    @Test
    public void verifyTypeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(DuplicateName.class);
    }
}

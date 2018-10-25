package com.vimeo.sample_java_model;

import org.junit.Test;

import verification.Utils;

/**
 * Tests for {@link PrivateMembers}.
 * <p>
 * Created by anthonycr on 5/16/17.
 */
public class PrivateMembersTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(PrivateMembers.class);
    }

}

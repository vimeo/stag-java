package com.vimeo.stag.processor;

import com.vimeo.stag.processor.utils.Preconditions;

import org.junit.Test;

public class PreconditionsUnitTest {

    @SuppressWarnings("ConstantConditions")
    @Test(expected = NullPointerException.class)
    public void checkNotNull_Null_throwsNullPointer() {
        Object o = null;
        Preconditions.checkNotNull(o);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void checkNotNull_NotNull() throws Exception {
        Object o = new Object();
        Preconditions.checkNotNull(o);
    }

}

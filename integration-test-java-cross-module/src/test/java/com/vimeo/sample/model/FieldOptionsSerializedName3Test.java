package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.fieldoption.FieldOptionsSerializedName3;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class FieldOptionsSerializedName3Test {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(FieldOptionsSerializedName3.class);
    }

}
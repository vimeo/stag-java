package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.scenarios.NestedModel;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class NestedModelTest {

    @Test
    public void typeAdapterWasGenerated_NestedModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedModel.class);
    }

    @Test
    public void typeAdapterWasGenerated_NestedModel_NestedEnum() throws Exception {
        Utils.verifyTypeAdapterGeneration(NestedModel.NestedEnum.class);
    }

}
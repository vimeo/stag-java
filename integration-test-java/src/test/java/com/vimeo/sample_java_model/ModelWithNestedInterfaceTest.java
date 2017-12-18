package com.vimeo.sample_java_model;

import org.junit.Test;

/**
 * Created by restainoa on 2/3/17.
 */
public class ModelWithNestedInterfaceTest {

    @Test
    public void typeAdapterWasGenerated_ModelWithNestedInterface() throws Exception {
        Utils.verifyTypeAdapterGeneration(ModelWithNestedInterface.class);
    }

    @Test
    public void typeAdapterWasNotGenerated_ModelWithNestedInterface_NestedInterface() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(ModelWithNestedInterface.NestedInterface.class);
    }

    @Test
    public void typeAdapterWasNotGenerated_ModelWithNestedInterface_NestedInterface_NestedModel()
            throws Exception {
        Utils.verifyNoTypeAdapterGeneration(ModelWithNestedInterface.NestedInterface.NestedModel.class);
    }

}
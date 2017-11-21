package com.vimeo.sample.model;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.scenarios.ConcreteClass;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class ConcreteClassTest {

    @Test
    public void typeAdapterWasGenerated_ConcreteClass() throws Exception {
        Utils.verifyTypeAdapterGeneration(ConcreteClass.class);
    }

    @Test
    public void typeAdapterWasGenerated_ConcreteClass_NestedModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(ConcreteClass.NestedModel.class);
    }

    @Test
    public void typeAdapterWasGenerated_ConcreteClass_NestedModel_DoublyNestedModel() throws Exception {
        Utils.verifyTypeAdapterGeneration(ConcreteClass.NestedModel.DoublyNestedModel.class);
    }

}

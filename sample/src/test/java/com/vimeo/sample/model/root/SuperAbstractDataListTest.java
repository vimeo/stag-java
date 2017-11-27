package com.vimeo.sample.model.root;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.root.SuperAbstractDataList;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class SuperAbstractDataListTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(SuperAbstractDataList.class);
    }

}
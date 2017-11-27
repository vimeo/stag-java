package com.vimeo.sample.model.root;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.root.AbstractDataList;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class AbstractDataListTest {

    @Test
    public void typeAdapterWasNotGenerated() throws Exception {
        Utils.verifyNoTypeAdapterGeneration(AbstractDataList.class);
    }

}

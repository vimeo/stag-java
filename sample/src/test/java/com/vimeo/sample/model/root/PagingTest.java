package com.vimeo.sample.model.root;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.root.Paging;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class PagingTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(Paging.class);
    }

}
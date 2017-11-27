package com.vimeo.sample.model.root;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.VideoList;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class VideoListTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(VideoList.class);
    }

}
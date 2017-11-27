package com.vimeo.sample.model.root;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.Video;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class VideoTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(Video.class);
    }

}
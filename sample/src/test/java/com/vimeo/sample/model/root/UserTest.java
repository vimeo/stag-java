package com.vimeo.sample.model.root;

import com.vimeo.sample.Utils;
import com.vimeo.sample.model.root.User;

import org.junit.Test;

/**
 * Created by restainoa on 2/2/17.
 */
public class UserTest {

    @Test
    public void typeAdapterWasGenerated() throws Exception {
        Utils.verifyTypeAdapterGeneration(User.class);
    }

}
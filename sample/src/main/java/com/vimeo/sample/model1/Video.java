package com.vimeo.sample.model1;

import com.vimeo.sample.model.User;
import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.UseStag;

/**
 * This class has been added to repro the scenario where two classes with same names exists in different packages.
 * This will make sure that the getter type adapter method for these classes are generated with the correct name (appending package name)
 * so that two getters with same name is not created.
 * <p>
 * This class has been referenced as a member variable in {@link com.vimeo.sample.model.Video} class
 */
@UseStag
public class Video {

    @GsonAdapterKey("user")
    public User mUser;

    @GsonAdapterKey("link")
    public String mLink;

    @GsonAdapterKey("mData")
    public Data mData;
}
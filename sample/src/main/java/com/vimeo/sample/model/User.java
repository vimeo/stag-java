package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

public class User {

    @GsonAdapterKey("name")
    public String mName;

    @GsonAdapterKey("location")
    public String mLocation;

    @Override
    public String toString() {
        return "name: " + mName + ", location: " + mLocation;
    }
}

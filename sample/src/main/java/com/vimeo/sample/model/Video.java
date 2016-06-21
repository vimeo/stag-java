package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

public class Video {

    @GsonAdapterKey("user")
    public User mUser;

    @GsonAdapterKey("link")
    public String mLink;

    @GsonAdapterKey("name")
    public String mName;

    @GsonAdapterKey("created_time")
    public String mCreatedTime;

    @GsonAdapterKey("stats")
    public Stats mStats;

    @Override
    public String toString() {
        return "user: { " + (mUser != null ? mUser.toString() : null) + " }\nlink: " + mLink + "\nname: " +
               mName + "\ncreated_time: " + mCreatedTime + "\nstats: { " +
               (mStats != null ? mStats.toString() : null) + " }";
    }
}

package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

public class Stats {

    @GsonAdapterKey("plays")
    public int mPlays;


    @Override
    public String toString() {
        return "plays: " + mPlays;
    }
}

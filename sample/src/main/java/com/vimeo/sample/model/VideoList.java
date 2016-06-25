package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

import java.util.ArrayList;
import java.util.List;

public class VideoList {

    @GsonAdapterKey
    public ArrayList<Video> data;

}

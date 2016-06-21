package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

import java.util.List;

public class VideoList {

    @GsonAdapterKey("data")
    public List<Video> mVideoList;

}

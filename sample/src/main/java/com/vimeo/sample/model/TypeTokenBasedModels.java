package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

import java.util.List;
import java.util.Map;

public class TypeTokenBasedModels {

    @GsonAdapterKey("videoMap")
    public Map<String, Video> videoMap;

    @GsonAdapterKey("videoList")
    public List<Video> videoList;
}

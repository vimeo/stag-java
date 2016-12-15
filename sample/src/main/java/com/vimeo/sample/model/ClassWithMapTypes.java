package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ClassWithMapTypes {
    @GsonAdapterKey
    public HashMap<String, Video> videoHashMap;

    @GsonAdapterKey
    public LinkedHashMap<String, Integer> stringIntegerMap;

    @GsonAdapterKey
    public Map<Integer, String> integerStringMap;

    @GsonAdapterKey
    public Map<Video, String> complexTypeMap;

    @GsonAdapterKey
    public Map<String, List<Video>> mapOfLists;

}

package com.vimeo.sample.model;


import com.vimeo.stag.UseStag;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@UseStag
public class ClassWithMapTypes {
    public HashMap<String, Video> videoHashMap;

    public LinkedHashMap<String, Integer> stringIntegerMap;

    public Map<Integer, String> integerStringMap;

    public Map<Video, String> complexTypeMap;

    public Map<String, List<Video>> mapOfLists;

}

package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import java.util.Map;

/**
 * Created by anirudh.r on 30/11/16.
 */
@UseStag
public class GenericClass<T> {

    @SerializedName("name")
    public T name;

    @SerializedName("map")
    public Map<String, Map<String, T>> mapField;
}
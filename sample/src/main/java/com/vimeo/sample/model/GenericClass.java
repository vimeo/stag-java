package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

import java.util.Map;

/**
 * Created by anirudh.r on 30/11/16.
 */

public class GenericClass<T> {

    @GsonAdapterKey
    public T name;

    @GsonAdapterKey
    public Map<String, Map<String, T>> mapField;
}
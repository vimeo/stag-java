package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

public class SuperAbstractDataList<T, K> {

    @GsonAdapterKey
    public T paging;

    @GsonAdapterKey
    public K data;

}

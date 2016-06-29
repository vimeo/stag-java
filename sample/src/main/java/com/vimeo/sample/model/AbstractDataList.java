package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

import java.util.ArrayList;

public class AbstractDataList<T> extends SuperAbstractDataList<Paging, ArrayList<T>> {

    @GsonAdapterKey
    public int page;

}

package com.vimeo.sample.model;

import com.vimeo.stag.UseStag;

@UseStag
public class DynamicallyTypedModel<T extends Video, V extends AbstractDataList<T>> {
    public T videoValue;
    public V dataListValue;
}

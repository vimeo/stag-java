package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

@UseStag
public class DynamicallyTypedModel<T extends BaseExternalModel, V extends ExternalModelGeneric<T>> {
    public T videoValue;
    public V dataListValue;
}

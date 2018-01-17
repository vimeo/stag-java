package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

/**
 * A model with multiple generic types. Acting as a use case for {@link DynamicallyTypedWildcard}.
 */
@UseStag
public class DynamicallyTypedModel<T extends BaseExternalModel, V extends ExternalModelGeneric<T>> {

    public T videoValue;

    public V dataListValue;

}

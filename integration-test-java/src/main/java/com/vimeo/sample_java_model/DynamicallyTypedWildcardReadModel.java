package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

import java.util.List;

/**
 * Model which references a generically typed object using wildcard bounds.
 */
@UseStag
public class DynamicallyTypedWildcardReadModel {

    public List<DynamicallyTypedModel<?>> models;

}

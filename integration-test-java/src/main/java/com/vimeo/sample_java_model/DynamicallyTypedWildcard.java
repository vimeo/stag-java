package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

import java.util.List;

/**
 * A test case which tests a generic type that has wildcards. The type used is {@link DynamicallyTypedModel}.
 */
@UseStag
public class DynamicallyTypedWildcard {

    public List<DynamicallyTypedModel<?, ?>> models;

    public String name;

}

package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

import java.util.List;

@UseStag
public class DynamicallyTypedWildcard {
    public List<DynamicallyTypedModel<?, ?>> models;
    public String name;
}

package com.vimeo.sample.model;

import com.vimeo.stag.UseStag;

import java.util.List;

@UseStag
public class DynamicallyTypedWildcardReadModel {
    public List<DynamicallyTypedModel<?>> models;
    public String name;
}

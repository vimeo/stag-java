package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

@UseStag
public class WildcardModel {

    public String name;

    public ExternalModelGeneric<?> externalModelGeneric;
}

package com.vimeo.sample_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public abstract class ExternalAbstractClass {

    @SerializedName("type")
    public String mType;

    @SerializedName("externalModel2")
    public ExternalModel2 mExternalModel2;
}
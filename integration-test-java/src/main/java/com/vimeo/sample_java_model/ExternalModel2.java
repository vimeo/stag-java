package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModel2 {

    @SerializedName("externalModelString")
    public String mExternalModelString;
}
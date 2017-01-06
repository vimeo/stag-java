package com.vimeo.sample_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModel1 {

    @SerializedName("field1")
    public String mField1;

    @SerializedName("genericField")
    public ExternalModelGeneric<String> mGenericField;
}
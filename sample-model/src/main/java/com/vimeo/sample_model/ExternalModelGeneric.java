package com.vimeo.sample_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModelGeneric<T> {

    @SerializedName("field2")
    public String mField2;

    @SerializedName("genericField")
    public T mGenericField;
}

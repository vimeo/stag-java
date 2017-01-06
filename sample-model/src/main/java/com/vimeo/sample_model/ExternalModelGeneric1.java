package com.vimeo.sample_model;

import android.webkit.ValueCallback;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModelGeneric1<T> {

    @SerializedName("field2")
    public String mField2;

    @SerializedName("genericField")
    public T mGenericField;

    @SerializedName("unkownType")
    public ValueCallback<T> mUnknownType;
}
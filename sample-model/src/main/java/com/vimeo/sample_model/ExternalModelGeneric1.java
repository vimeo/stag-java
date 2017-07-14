package com.vimeo.sample_model;

import android.webkit.ValueCallback;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModelGeneric1<T> {

    @SerializedName("field2")
    private String mField2;

    @SerializedName("genericField")
    private T mGenericField;

    @SerializedName("unknownType")
    private ValueCallback<T> mUnknownType;

    public String getField2() {
        return mField2;
    }

    public void setField2(String field2) {
        mField2 = field2;
    }

    public T getGenericField() {
        return mGenericField;
    }

    public void setGenericField(T genericField) {
        mGenericField = genericField;
    }

    public ValueCallback<T> getUnknownType() {
        return mUnknownType;
    }

    public void setUnknownType(ValueCallback<T> unknownType) {
        mUnknownType = unknownType;
    }
}
package com.vimeo.integration_test_android;

import android.webkit.ValueCallback;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * A class that contains a type which is unserializable.
 */
@UseStag
public class UnserializablePlatformType<T> {

    @SerializedName("unknownType")
    private ValueCallback<T> mUnknownType;

    public ValueCallback<T> getUnknownType() {
        return mUnknownType;
    }

    public void setUnknownType(ValueCallback<T> unknownType) {
        mUnknownType = unknownType;
    }
}

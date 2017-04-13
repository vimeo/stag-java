package com.vimeo.sample.model1;

import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This model does not use stag.
 * As it is not of paramtereized type, this will use TypeToken.get()
 */
public class Data {

    @SerializedName("size")
    public int size;

    @Nullable
    @SerializedName("name")
    public String name;
}
package com.vimeo.sample.model1;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This model does not use stag
 */
public class Data {

    @NonNull
    @SerializedName("size")
    public int size;

    @Nullable
    @SerializedName("name")
    public String name;
}
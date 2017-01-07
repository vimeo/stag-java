package com.vimeo.sample.model1;

import com.google.gson.annotations.SerializedName;

/**
 * This model does not use stag
 */
public class Data {

    @SerializedName("size")
    public int size;

    @SerializedName("name")
    public String name;
}
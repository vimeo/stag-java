package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;

/*
    This is to simulate a scenario where a class with the same name is present as
    a static inner class for another class in the same package
 */

public class NestedModel {

    @SerializedName("test1")
    public String test1;
}

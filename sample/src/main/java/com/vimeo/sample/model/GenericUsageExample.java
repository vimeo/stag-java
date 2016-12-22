package com.vimeo.sample.model;


import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class GenericUsageExample {

    @SerializedName("GenericUsage")
    GenericClass<GenericClass<User>> mGenericUsage;
}

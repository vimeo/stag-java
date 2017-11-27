package com.vimeo.sample_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public enum AlternateNameModel1 {
    @SerializedName(value = "7.0", alternate = {"4.0", "5.0"})
    ANDROID_VERSION,

    @SerializedName(value = "Nougat", alternate = {"Kitkat", "Lollipop"})
    ANDROID_VERSION_NAME
}
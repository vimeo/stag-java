package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class AlternateNameModel {

    @SerializedName(value = "Nougat", alternate = {"Kitkat", "Lollipop"})
    String mAndroidVersions;

    @SerializedName(value = "7.0", alternate = {"4.0", "5.0"})
    String mAndroidNameVersions;
}
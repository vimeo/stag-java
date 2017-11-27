package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class AlternateNameModel {

    @SerializedName(value = "Nougat", alternate = {"Kitkat", "Lollipop"})
    private String mAndroidVersions;

    @SerializedName(value = "7.0", alternate = {"4.0", "5.0"})
    private String mAndroidNameVersions;

    public String getAndroidVersions() {
        return mAndroidVersions;
    }

    public void setAndroidVersions(String androidVersions) {
        mAndroidVersions = androidVersions;
    }

    public String getAndroidNameVersions() {
        return mAndroidNameVersions;
    }

    public void setAndroidNameVersions(String androidNameVersions) {
        mAndroidNameVersions = androidNameVersions;
    }
}
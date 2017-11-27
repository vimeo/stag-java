package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModel2 {

    @SerializedName("externalModelString") private String mExternalModelString;

    public String getExternalModelString() {
        return mExternalModelString;
    }

    public void setExternalModelString(String externalModelString) {
        mExternalModelString = externalModelString;
    }
}
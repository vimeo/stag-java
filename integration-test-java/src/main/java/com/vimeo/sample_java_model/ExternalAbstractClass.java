package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public abstract class ExternalAbstractClass {

    @SerializedName("type")
    private String mType;

    @SerializedName("externalModel2")
    private ExternalModel2 mExternalModel2;

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

    public ExternalModel2 getExternalModel2() {
        return mExternalModel2;
    }

    public void setExternalModel2(ExternalModel2 externalModel2) {
        mExternalModel2 = externalModel2;
    }
}
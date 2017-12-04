package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class BaseExternalModel {

    @SerializedName("type")
    private String mType;

    @SerializedName("base_value")
    private int mBaseValue;

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public int getBaseValue() {
        return mBaseValue;
    }

    public void setBaseValue(int baseValue) {
        this.mBaseValue = baseValue;
    }
}

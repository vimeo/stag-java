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

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        BaseExternalModel that = (BaseExternalModel) o;

        if (mBaseValue != that.mBaseValue) { return false; }
        return mType != null ? mType.equals(that.mType) : that.mType == null;
    }

    @Override
    public int hashCode() {
        int result = mType != null ? mType.hashCode() : 0;
        result = 31 * result + mBaseValue;
        return result;
    }
}

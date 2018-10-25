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

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        AlternateNameModel that = (AlternateNameModel) o;

        if (mAndroidVersions != null ? !mAndroidVersions.equals(that.mAndroidVersions) : that.mAndroidVersions != null) {
            return false;
        }
        return mAndroidNameVersions != null ? mAndroidNameVersions.equals(that.mAndroidNameVersions) : that.mAndroidNameVersions == null;
    }

    @Override
    public int hashCode() {
        int result = mAndroidVersions != null ? mAndroidVersions.hashCode() : 0;
        result = 31 * result + (mAndroidNameVersions != null ? mAndroidNameVersions.hashCode() : 0);
        return result;
    }
}

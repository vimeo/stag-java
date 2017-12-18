package com.vimeo.sample.model.basic;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import java.util.List;

/**
 * A basic example of usage.
 */
@UseStag
public class BasicModel1 {

    @SerializedName("name")
    private String mName;

    @SerializedName("age")
    private int mAge;

    @SerializedName("awards")
    private List<String> mAwards;

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getAge() {
        return mAge;
    }

    public void setAge(int age) {
        mAge = age;
    }

    public List<String> getAwards() {
        return mAwards;
    }

    public void setAwards(List<String> awards) {
        mAwards = awards;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        BasicModel1 that = (BasicModel1) o;

        if (mAge != that.mAge) { return false; }
        if (mName != null ? !mName.equals(that.mName) : that.mName != null) { return false; }
        return mAwards != null ? mAwards.equals(that.mAwards) : that.mAwards == null;
    }

    @Override
    public int hashCode() {
        int result = mName != null ? mName.hashCode() : 0;
        result = 31 * result + mAge;
        result = 31 * result + (mAwards != null ? mAwards.hashCode() : 0);
        return result;
    }
}

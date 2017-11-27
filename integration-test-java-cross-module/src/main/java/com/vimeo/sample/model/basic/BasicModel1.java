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
}

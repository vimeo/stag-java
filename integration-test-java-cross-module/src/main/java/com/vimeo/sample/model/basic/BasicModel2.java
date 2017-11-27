package com.vimeo.sample.model.basic;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * Another basic example of usage.
 */
@UseStag
public class BasicModel2 {

    @SerializedName("name")
    private int mTitle;

    @SerializedName("duration")
    private int mDuration;

    @SerializedName("score")
    private Integer mScore;

    public int getTitle() {
        return mTitle;
    }

    public void setTitle(int title) {
        mTitle = title;
    }

    public int getDuration() {
        return mDuration;
    }

    public void setDuration(int duration) {
        mDuration = duration;
    }

    public Integer getScore() {
        return mScore;
    }

    public void setScore(Integer score) {
        mScore = score;
    }
}

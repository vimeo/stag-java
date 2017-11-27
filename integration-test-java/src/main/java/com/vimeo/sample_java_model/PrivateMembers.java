package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import org.jetbrains.annotations.NotNull;

/**
 * Model testing private members with public setters.
 * <p>
 * Created by anthonycr on 5/16/17.
 */
@UseStag
public class PrivateMembers {

    @NotNull
    private String mTestString;

    @SerializedName("testString2")
    private String mAnotherTestString;

    private Object mTestObject;

    @NotNull
    public String getTestString() {
        return mTestString;
    }

    public void setTestString(@NotNull String testString) {
        this.mTestString = testString;
    }

    public String getAnotherTestString() {
        return mAnotherTestString;
    }

    public void setAnotherTestString(String anotherTestString) {
        this.mAnotherTestString = anotherTestString;
    }

    public Object getTestObject() {
        return mTestObject;
    }

    public void setTestObject(Object testObject) {
        this.mTestObject = testObject;
    }
}

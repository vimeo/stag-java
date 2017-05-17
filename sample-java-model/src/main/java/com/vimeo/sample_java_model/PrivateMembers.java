package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import org.jetbrains.annotations.NotNull;

/**
 * Model testing private members with
 * public setters.
 * <p>
 * Created by anthonycr on 5/16/17.
 */
@UseStag
public class PrivateMembers {

    @NotNull
    private String testString;

    @SerializedName("testString2")
    private String anotherTestString;

    private Object testObject;

    @NotNull
    public String getTestString() {
        return testString;
    }

    public void setTestString(@NotNull String testString) {
        this.testString = testString;
    }

    public String getAnotherTestString() {
        return anotherTestString;
    }

    public void setAnotherTestString(String anotherTestString) {
        this.anotherTestString = anotherTestString;
    }

    public Object getTestObject() {
        return testObject;
    }

    public void setTestObject(Object testObject) {
        this.testObject = testObject;
    }
}

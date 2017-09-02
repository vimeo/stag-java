package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

import org.jetbrains.annotations.NotNull;

/**
 * A test case for the various naming options for booleans.
 * <p>
 * Created by anthonycr on 9/2/17.
 */
@UseStag
public class BooleanFields {

    @SerializedName("test1")
    private boolean mTest1;

    @SerializedName("test2")
    private boolean mTest2;

    @NotNull
    @SerializedName("test3")
    private Boolean mTest3;

    @NotNull
    @SerializedName("test4")
    private Boolean mTest4;

    @SerializedName("test5")
    private boolean mIsTest5;

    @SerializedName("test6")
    private boolean mIsTest6;

    @NotNull
    @SerializedName("test7")
    private Boolean mIsTest7;

    @NotNull
    @SerializedName("test8")
    private Boolean mIsTest8;

    public boolean isTest1() {
        return mTest1;
    }

    public void setTest1(boolean mTest1) {
        this.mTest1 = mTest1;
    }

    public boolean getTest2() {
        return mTest2;
    }

    public void setTest2(boolean mTest2) {
        this.mTest2 = mTest2;
    }

    @NotNull
    public Boolean isTest3() {
        return mTest3;
    }

    public void setTest3(@NotNull Boolean mTest3) {
        this.mTest3 = mTest3;
    }

    @NotNull
    public Boolean getTest4() {
        return mTest4;
    }

    public void setTest4(@NotNull Boolean mTest4) {
        this.mTest4 = mTest4;
    }

    public boolean isTest5() {
        return mIsTest5;
    }

    public void setIsTest5(boolean mIsTest5) {
        this.mIsTest5 = mIsTest5;
    }

    public boolean getIsTest6() {
        return mIsTest6;
    }

    public void setIsTest6(boolean mIsTest6) {
        this.mIsTest6 = mIsTest6;
    }

    @NotNull
    public Boolean isTest7() {
        return mIsTest7;
    }

    public void setIsTest7(@NotNull Boolean mIsTest7) {
        this.mIsTest7 = mIsTest7;
    }

    @NotNull
    public Boolean getIsTest8() {
        return mIsTest8;
    }

    public void setIsTest8(@NotNull Boolean mIsTest8) {
        this.mIsTest8 = mIsTest8;
    }
}

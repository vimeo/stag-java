package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * A model to test identically named models in different packages.
 */
@UseStag
public class DuplicateName {

    @SerializedName("field_1")
    private String mField1;

    @SerializedName("field_2")
    private String mField2;

    @SerializedName("field_3")
    private int mField3;

    @SerializedName("field_4")
    private com.vimeo.sample.model1.DuplicateName mField4;

    public String getField1() {
        return mField1;
    }

    public void setField1(String field1) {
        mField1 = field1;
    }

    public String getField2() {
        return mField2;
    }

    public void setField2(String field2) {
        mField2 = field2;
    }

    public int getField3() {
        return mField3;
    }

    public void setField3(int field3) {
        mField3 = field3;
    }

    public com.vimeo.sample.model1.DuplicateName getField4() {
        return mField4;
    }

    public void setField4(com.vimeo.sample.model1.DuplicateName field4) {
        mField4 = field4;
    }
}

package com.vimeo.integration_test_android;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * A simple model used in {@link ClassWithMapTypes}.
 */
@UseStag
public class TestModel {

    @SerializedName("field_1")
    private String mField1;

    @SerializedName("field_2")
    private int mField2;

    public String getField1() {
        return mField1;
    }

    public void setField1(String field1) {
        mField1 = field1;
    }

    public int getField2() {
        return mField2;
    }

    public void setField2(int field2) {
        mField2 = field2;
    }
}

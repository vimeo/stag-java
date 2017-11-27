package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModel1 {

    @SerializedName("field1")
    public String mField1;

    @SerializedName("genericField")
    public ExternalModelGeneric<String> mGenericField;

    public String getField1() {
        return mField1;
    }

    public void setField1(String field1) {
        mField1 = field1;
    }

    public ExternalModelGeneric<String> getGenericField() {
        return mGenericField;
    }

    public void setGenericField(ExternalModelGeneric<String> genericField) {
        mGenericField = genericField;
    }
}
package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * An enum with a field.
 */
@UseStag
public enum EnumWithFieldsModel {
    ENUM_1("test_1"),
    ENUM_2("test_2");

    @SerializedName("field")
    private final String mField;

    EnumWithFieldsModel(String field) {
        this.mField = field;
    }

    public String getField() {
        return mField;
    }

}

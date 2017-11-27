package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

/**
 * An enum with a field.
 */
@UseStag
public enum EnumWithFieldsModel {
    ENUM_1("test_1"),
    ENUM_2("test_2");

    private final String field;

    EnumWithFieldsModel(String field) {
        this.field = field;
    }

    public String getField() {
        return field;
    }

}

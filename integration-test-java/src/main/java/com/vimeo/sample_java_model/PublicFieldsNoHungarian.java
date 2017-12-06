package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * A test case where all the fields are public and hungarian notation is not used.
 */
@UseStag
public class PublicFieldsNoHungarian {

    public String name;

    @SerializedName("name_2")
    public String anotherField;
}

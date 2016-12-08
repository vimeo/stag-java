package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

@UseStag
public enum EnumExample {
    ENUM_VALUE1,
    ENUM_VALUE2,
    ENUM_VALUE3,
    @SerializedName("CUSTOM_ENUM_VALUE4")
    ENUM_VALUE4,
    ENUM_VALUE5,
    ENUM_VALUE6

}

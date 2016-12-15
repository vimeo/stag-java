package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

@GsonAdapterKey
public enum EnumExample {
    ENUM_VALUE1,
    ENUM_VALUE2,
    ENUM_VALUE3,
    @GsonAdapterKey("CUSTOM_ENUM_VALUE4")
    ENUM_VALUE4,
    ENUM_VALUE5,
    ENUM_VALUE6

}

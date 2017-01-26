package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.GsonAdapterKey;
import com.vimeo.stag.UseStag;

/**
 * Model class which contains two annotations
 * {@link com.vimeo.stag.GsonAdapterKey}
 * {@link com.google.gson.annotations.SerializedName}
 * <p>
 * If we have this kind of scenario, we will give preference to {@link com.vimeo.stag.GsonAdapterKey} value
 */
@UseStag
public class MultipleAnnotationSupport {

    @GsonAdapterKey("gsonAdapterValue")
    @SerializedName("serializedNameValue")
    public int mValue;

    @GsonAdapterKey("gsonAdapterName")
    @SerializedName("serializedNameName")
    public String mName;
}
package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.sample_model.ExternalAbstractClass;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModelExample3 extends ExternalAbstractClass {

    @SerializedName("mainModuleValue")
    public String mMainModuleValue;

}

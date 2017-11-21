package com.vimeo.sample.model.scenarios;


import android.support.annotation.Nullable;

import com.vimeo.stag.UseStag;

@UseStag
public class ClassWithNestedInterface {

    public @interface AvailabilityIntentDef {
    }

    @Nullable
    public String displayState;

    public boolean showMessage;
}

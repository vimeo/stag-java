package com.vimeo.sample.model;

import com.vimeo.sample_model.ExternalModel1;
import com.vimeo.stag.UseStag;

/**
 * Testing adapter creation for a model class
 * that relies on an externally defined class.
 * <p>
 * Created by mohammad.yasir on 28/12/16.
 */

@UseStag
public class TestExternalExample {

    public GenericClass<ExternalModel1> mTesting;
}

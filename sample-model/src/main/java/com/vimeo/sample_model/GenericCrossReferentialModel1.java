package com.vimeo.sample_model;

import com.vimeo.stag.UseStag;

/**
 * Created by restainoa on 2/4/17.
 */
@UseStag
public class GenericCrossReferentialModel1<T> {

    T field1;

    GenericCrossReferentialModel2<T> genericField;

}
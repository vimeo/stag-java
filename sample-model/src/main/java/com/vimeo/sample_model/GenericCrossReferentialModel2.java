package com.vimeo.sample_model;

import com.vimeo.stag.UseStag;

import java.util.List;

/**
 * Created by restainoa on 2/4/17.
 */
@UseStag
public class GenericCrossReferentialModel2<T> {

    List<T> listOfT;

    GenericCrossReferentialModel1<T> genericField;

}

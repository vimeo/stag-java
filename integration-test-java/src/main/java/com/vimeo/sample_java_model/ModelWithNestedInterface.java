package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

/**
 * Created by restainoa on 2/3/17.
 */
@UseStag
public class ModelWithNestedInterface {

    protected String field;

    public interface NestedInterface {

        class NestedModel {

            protected String otherField;

        }

    }

}

package com.vimeo.sample.model.scenarios;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;

/**
 * Created by restainoa on 2/2/17.
 */
@UseStag
public interface SampleInterface {

    void sample();

    class NestedClass {

        protected String field;
    }

    @UseStag(FieldOption.SERIALIZED_NAME)
    class NestedClassAnnotated {

        @SerializedName("field")
        protected String field;
    }

}

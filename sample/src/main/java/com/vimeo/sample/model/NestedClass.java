package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;

@UseStag
public class NestedClass {

    /**
     * This class should have a TypeAdapter created
     * for it with FieldOption.ALL
     */
    public static class Nested extends NestedClass {

    }

    @UseStag(FieldOption.SERIALIZED_NAME)
    public static class NestedWithAnnotation {

        /**
         * This class should not have a TypeAdapter created, even though it is nested within
         * an annotated class.
         */
        public static class NestedWithoutAnnotation {

            // won't be picked up by stag because it inherits the parent annotation
            protected String field;

            @SerializedName("name")
            protected String field1;

        }

    }
}
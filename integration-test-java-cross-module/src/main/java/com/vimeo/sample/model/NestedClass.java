package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;
import com.vimeo.stag.UseStag.FieldOption;

@UseStag
public class NestedClass {

    /**
     * This class is not directly annotated but the parent class is annotated.
     * We have a choice here as to whether or not we automatically generate type adapters
     * for such classes.  We're opting for the principle of least surprise and requiring all
     * classes to explicitly specify the annotation in order to enable generation.
     */
    public static class NestedExtension extends NestedClass {

        String field;
    }

    /**
     * This class is unannotated and does not inherit from an annotated type.
     */
    public static class Nested {

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

package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

import org.jetbrains.annotations.NotNull;

/**
 * A model to test the case where the consumer
 * should be able to use different parsers for
 * an object across different gson instances.
 */
@UseStag
public class SwappableParserExampleModel {

    @NotNull
    String testField1;

    @NotNull
    TestObject testField2;

    public static class TestObject {

        @NotNull
        String testField;

    }

}

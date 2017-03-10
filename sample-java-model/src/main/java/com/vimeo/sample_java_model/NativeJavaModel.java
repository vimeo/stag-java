package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

@UseStag
public class NativeJavaModel {

    String topLevel;

    @UseStag
    public static class Nested {
        String nested;
    }

    public static class NestedWithoutAnnotation {
        String nestedWithoutAnnotation;
    }

    @UseStag
    public static class NestedExtension extends NativeJavaModel {
        String nestedExtension;
    }

    public static class NestedExtensionWithoutAnnotation extends NativeJavaModel {
        String nestedExtensionWithoutAnnotation;
    }

}

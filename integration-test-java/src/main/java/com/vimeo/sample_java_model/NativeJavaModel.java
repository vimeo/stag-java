package com.vimeo.sample_java_model;

import com.vimeo.stag.UseStag;

@UseStag
public class NativeJavaModel {

    private String mTopLevel;

    public String getTopLevel() {
        return mTopLevel;
    }

    public void setTopLevel(String mTopLevel) {
        this.mTopLevel = mTopLevel;
    }

    @UseStag
    public static class Nested {
        private String mNested;

        public String getNested() {
            return mNested;
        }

        public void setNested(String mNested) {
            this.mNested = mNested;
        }
    }

    public static class NestedWithoutAnnotation {
        private String mNestedWithoutAnnotation;

        public String getNestedWithoutAnnotation() {
            return mNestedWithoutAnnotation;
        }

        public void setNestedWithoutAnnotation(String mNestedWithoutAnnotation) {
            this.mNestedWithoutAnnotation = mNestedWithoutAnnotation;
        }
    }

    @UseStag
    public static class NestedExtension extends NativeJavaModel {
        private String mNestedExtension;

        public String getNestedExtension() {
            return mNestedExtension;
        }

        public void setNestedExtension(String mNestedExtension) {
            this.mNestedExtension = mNestedExtension;
        }
    }

    public static class NestedExtensionWithoutAnnotation extends NativeJavaModel {
        private String mNestedExtensionWithoutAnnotation;

        public String getNestedExtensionWithoutAnnotation() {
            return mNestedExtensionWithoutAnnotation;
        }

        public void setNestedExtensionWithoutAnnotation(String mNestedExtensionWithoutAnnotation) {
            this.mNestedExtensionWithoutAnnotation = mNestedExtensionWithoutAnnotation;
        }
    }

    @UseStag
    public static class NestedExtensionFromNoAnnotation extends NestedWithoutAnnotation {
        private String mNestedExtensionFromNoAnnotation;

        public String getNestedExtensionFromNoAnnotation() {
            return mNestedExtensionFromNoAnnotation;
        }

        public void setNestedExtensionFromNoAnnotation(String mNestedExtensionFromNoAnnotation) {
            this.mNestedExtensionFromNoAnnotation = mNestedExtensionFromNoAnnotation;
        }
    }

}

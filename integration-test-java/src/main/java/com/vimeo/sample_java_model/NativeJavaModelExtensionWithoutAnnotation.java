package com.vimeo.sample_java_model;

/**
 * This class is separate/redundant with respect to those in NestedJavaModel so that we can
 * observe the behavior of incremental compilation via manual manipulation.
 */
public class NativeJavaModelExtensionWithoutAnnotation extends NativeJavaModel {

    private String mAdditionalField2;

    public String getAdditionalField2() {
        return mAdditionalField2;
    }

    public void setAdditionalField2(String mAdditionalField2) {
        this.mAdditionalField2 = mAdditionalField2;
    }
}

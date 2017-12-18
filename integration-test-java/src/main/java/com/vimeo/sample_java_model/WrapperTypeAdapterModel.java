package com.vimeo.sample_java_model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * A model used to verify that wrapper type adapters are correctly called.
 * <p>
 * Created by restainoa on 12/18/17.
 */
@UseStag
public class WrapperTypeAdapterModel {

    @SerializedName("inner_type")
    private InnerType mInnerType;

    public InnerType getInnerType() {
        return mInnerType;
    }

    public void setInnerType(InnerType innerType) {
        mInnerType = innerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        WrapperTypeAdapterModel that = (WrapperTypeAdapterModel) o;

        return mInnerType != null ? mInnerType.equals(that.mInnerType) : that.mInnerType == null;
    }

    @Override
    public int hashCode() {
        return mInnerType != null ? mInnerType.hashCode() : 0;
    }

    @UseStag
    public static class InnerType {

        @SerializedName("hello")
        private String mHello;

        public String getHello() {
            return mHello;
        }

        public void setHello(String hello) {
            mHello = hello;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            InnerType innerType = (InnerType) o;

            return mHello != null ? mHello.equals(innerType.mHello) : innerType.mHello == null;
        }

        @Override
        public int hashCode() {
            return mHello != null ? mHello.hashCode() : 0;
        }
    }

}

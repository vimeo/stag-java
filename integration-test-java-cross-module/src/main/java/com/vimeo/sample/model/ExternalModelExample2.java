package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.sample_java_model.ExternalModel1;
import com.vimeo.sample_java_model.ExternalModelGeneric;
import com.vimeo.stag.UseStag;

@UseStag
public class ExternalModelExample2<T> {

    @SerializedName("stringField")
    public String mStringField;

    @SerializedName("externalExample")
    public ExternalModel1 mExternalModel;

    @SerializedName("externalGenericExample")
    public ExternalModelGeneric<String> mExternalGenericExample;

    @SerializedName("parametrizedExternalGenericExample")
    public ExternalModelGeneric<T> mParametrizedExternalGenericExample;


    @SerializedName("parametrizedInternalGenericExample")
    public GenericClass<T> mParametrizedInternalGenericExample;


    @SerializedName("parametrizedUnknownExternalGenericExample")
    public ExternalModelExample2<T> mParametrizedUnknownExternalGenericExample;
}

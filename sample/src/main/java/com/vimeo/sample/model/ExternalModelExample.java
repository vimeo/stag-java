package com.vimeo.sample.model;

import com.google.gson.annotations.SerializedName;
import com.vimeo.sample_model.ExternalModel1;
import com.vimeo.sample_model.ExternalModelGeneric;
import com.vimeo.stag.UseStag;

import java.util.Map;

@UseStag
public class ExternalModelExample<T> {

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


    @SerializedName("mapExternalModel")
    public Map<String, ExternalModel1> mMapExternalModel;


    @SerializedName("mapExternalGenericModel")
    public Map<String, ExternalModelGeneric<String>> mMapExternalGenericModel;

    @SerializedName("mapExternalGenericGenericModel")
    public Map<String, ExternalModelGeneric<T>> mMapExternalGenericGenericModel;

}

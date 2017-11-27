package com.vimeo.sample.model1;

import com.google.gson.annotations.SerializedName;
import com.vimeo.stag.UseStag;

/**
 * This class has been added to reproduce the scenario where two classes with same
 * names exists in different packages. This will make sure that the getter type
 * adapter method for these classes are generated with the correct name (appending
 * package name) so that two getters with same name is not created.
 * <p>
 * This class has been referenced as a member variable in {@link com.vimeo.sample.model.DuplicateName} class
 */
@UseStag
public class DuplicateName {

    @SerializedName("parameterized_data")
    private ParameterizedData<String> mParameterizedData;

    public ParameterizedData<String> getParameterizedData() {
        return mParameterizedData;
    }

    public void setParameterizedData(ParameterizedData<String> parameterizedData) {
        mParameterizedData = parameterizedData;
    }
}

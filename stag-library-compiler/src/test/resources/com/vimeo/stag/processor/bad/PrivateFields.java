package com.vimeo.stag.processor;

import com.vimeo.stag.UseStag;

@UseStag
public class PrivateFields {

    private String privateString;

    public PrivateFields()
    {
        privateString = null;
    }

    public PrivateFields(String privateString)
    {
        this.privateString = privateString;
    }

}
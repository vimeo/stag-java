package com.vimeo.sample.model;

import com.vimeo.stag.GsonAdapterKey;

/**
 * Entity ensuring that all supported modifiers are allowed.
 */
public class AccessModifiers {

    // private modifier is not allowed

    @GsonAdapterKey
    String defaultModifier;

    @GsonAdapterKey
    protected String protectedModifier;

    @GsonAdapterKey
    public String publicModifier;

}

package com.vimeo.sample.model;

import com.vimeo.stag.UseStag;

@UseStag
public enum NestedEnum {

    OUTER_SHOULD;

    /**
     * Nested definition without @UseStag should NOT generate type adapters.
     */
    public enum Nested {
        SHOULD_NOT
    }

    /**
     * Nested definition with @UseStag should generate type adapters.
     */
    @UseStag
    public enum NestedWithAnnotation {
        SHOULD
    }
}
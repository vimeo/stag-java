package com.vimeo.dummy.sample_kotlin

import com.google.gson.annotations.SerializedName
import com.vimeo.stag.UseStag

/**
 * Tests the use case of different named getters for booleans.
 *
 * Created by anthonycr on 9/2/17.
 */
@UseStag
class BoolFields {

    @SerializedName("test1")
    var test1: Boolean? = null

    @SerializedName("test2")
    var isTest2: Boolean? = null

}
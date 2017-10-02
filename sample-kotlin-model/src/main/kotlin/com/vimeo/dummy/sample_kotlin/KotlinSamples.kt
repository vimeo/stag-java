package com.vimeo.dummy.sample_kotlin

import com.google.gson.annotations.SerializedName
import com.vimeo.stag.UseStag

/**
 * Test kotlin model
 *
 * Created by restainoa on 5/8/17.
 */
@UseStag
data class KotlinSamples(
        var stringField: String? = null,
        var nonNullStringField: String = "default",
        var intField: Int? = null,
        var longField: Long = 1,
        @SerializedName("boolean_field") var booleanField: Boolean? = null
)
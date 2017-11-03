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
        @SerializedName("string_field") var stringField: String? = null,
        @SerializedName("non_null_string_field") var nonNullStringField: String = "default",
        @SerializedName("int_field") var intField: Int? = null,
        @SerializedName("long_field") var longField: Long = 1,
        @SerializedName("boolean_field") var booleanField: Boolean? = null,
        var notAnnotatedField: Int? = null // will still be picked up by the compiler and will look for json field named "nonAnnotatedField"
)
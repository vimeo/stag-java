package com.vimeo.dummy.sample_kotlin

import com.vimeo.stag.UseStag

/**
 * Kotlin type testing inheritance.
 *
 * Created by anthonycr on 5/18/17.
 */
@UseStag
class KotlinConcreteExample : KotlinGenericExample<String>() {

    var kotlinObject: KotlinSamples? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as KotlinConcreteExample

        if (kotlinObject != other.kotlinObject) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (kotlinObject?.hashCode() ?: 0)
        return result
    }

}
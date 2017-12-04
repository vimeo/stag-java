package com.vimeo.dummy.sample_kotlin

import com.vimeo.stag.UseStag

/**
 * Kotlin type testing abstract classes and generics.
 *
 * Created by anthonycr on 5/18/17.
 */
@UseStag
abstract class KotlinGenericExample<T> {

    var genericField: T? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KotlinGenericExample<*>

        if (genericField != other.genericField) return false

        return true
    }

    override fun hashCode(): Int {
        return genericField?.hashCode() ?: 0
    }

}

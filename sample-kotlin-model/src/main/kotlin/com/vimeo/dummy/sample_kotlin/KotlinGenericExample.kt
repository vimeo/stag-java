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

}

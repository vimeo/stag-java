package com.vimeo.dummy.sample_kotlin

import org.junit.Test

/**
 * Unit tests for [BoolFields].
 *
 * Created by anthonycr on 9/2/17.
 */
class BoolFieldsTest {

    @Test
    fun name() {
        Utils.verifyTypeAdapterGeneration(BoolFields::class)
    }

}
package com.vimeo.dummy.sample_kotlin

import org.junit.Test

/**
 * Unit tests for [BoolFields].
 *
 * Created by anthonycr on 9/2/17.
 */
class BoolFieldsTest {

    @Test
    fun verifyTypeAdapterWasGenerated() {
        Utils.verifyTypeAdapterGeneration(BoolFields::class)
    }

    @Test
    fun verifyTypeAdapterCorrect() {
        Utils.verifyTypeAdapterCorrectness(BoolFields::class)
    }
}
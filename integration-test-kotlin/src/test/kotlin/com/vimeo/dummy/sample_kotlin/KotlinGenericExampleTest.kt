package com.vimeo.dummy.sample_kotlin

import org.junit.Test

/**
 * Unit tests for [KotlinGenericExample]
 *
 * Created by anthonycr on 5/18/17.
 */
class KotlinGenericExampleTest {

    @Test
    fun verifyTypeAdapterGenerated() {
        Utils.verifyNoTypeAdapterGeneration(KotlinGenericExample::class)
    }
}
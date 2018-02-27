package com.vimeo.dummy.sample_kotlin

import org.junit.Test
import verification.Utils

/**
 * Unit tests for [KotlinConcreteExample].
 *
 * Created by anthonycr on 5/18/17.
 */
class KotlinConcreteExampleTest {

    @Test
    fun verifyTypeAdapterGenerated() {
        Utils.verifyTypeAdapterGeneration(KotlinConcreteExample::class)
    }

    @Test
    fun verifyTypeAdapterCorrectness() {
        Utils.verifyTypeAdapterCorrectness(KotlinConcreteExample::class)
    }
}

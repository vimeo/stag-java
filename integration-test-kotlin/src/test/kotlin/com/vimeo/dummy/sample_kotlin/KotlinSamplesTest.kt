package com.vimeo.dummy.sample_kotlin

import org.junit.Test
import verification.Utils

/**
 * Unit tests for [KotlinSamples].
 *
 * Created by restainoa on 5/8/17.
 */
class KotlinSamplesTest {

    @Test
    fun verifyTypeAdapterGenerated() {
        Utils.verifyTypeAdapterGeneration(KotlinSamples::class)
    }

    @Test
    fun verifyTypeAdapterCorrectness() {
        Utils.verifyTypeAdapterCorrectness(KotlinSamples::class)
    }
}

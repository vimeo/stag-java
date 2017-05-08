package com.vimeo.dummy.sample_kotlin

import org.junit.Test

/**
 * Unit test for KotlinSamples
 *
 * Created by restainoa on 5/8/17.
 */
class KotlinSamplesTest {

    @Test fun verifyTypeAdapterGenerated() {
        Utils.verifyTypeAdapterGeneration(KotlinSamples::class)
    }

}
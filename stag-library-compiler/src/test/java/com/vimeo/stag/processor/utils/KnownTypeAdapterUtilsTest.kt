package com.vimeo.stag.processor.utils

import com.vimeo.stag.processor.Utils
import org.junit.Test

/**
 * Unit tests for [KnownTypeAdapterUtils].
 *
 * Created by restainoa on 6/15/18.
 */
class KnownTypeAdapterUtilsTest {

    @Test
    fun `KnownTypeAdapterUtils is not instantiable`() {
        Utils.testZeroArgumentConstructorFinalClass(KnownTypeAdapterUtils::class.java)
    }

}

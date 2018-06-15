package com.vimeo.stag.processor.utils

import com.vimeo.stag.processor.Utils
import org.junit.Test

/**
 * Unit tests for [MessagerUtils].
 *
 * Created by restainoa on 6/15/18.
 */
class MessagerUtilsTest {

    @Test
    fun `MessagerUtils is not instantiable`() {
        Utils.testZeroArgumentConstructorFinalClass(MessagerUtils::class.java)
    }
}

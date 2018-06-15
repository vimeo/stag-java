package com.vimeo.stag.processor.utils

import com.vimeo.stag.processor.Utils
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.Closeable
import java.io.IOException

/**
 * Unit tests for [FileGenUtils].
 *
 * Created by restainoa on 6/15/18.
 */
class FileGenUtilsTest {

    @Test
    fun `FileGenUtils is not instantiable`() {
        Utils.testZeroArgumentConstructorFinalClass(FileGenUtils::class.java)
    }

    @Test
    fun close() {
        var counter = 0
        val closeable = Closeable { counter++ }
        FileGenUtils.close(closeable)
        assertEquals(1, counter)

        FileGenUtils.close(null)
        assertEquals(1, counter)

        val failureCloseable = Closeable { throw IOException("test") }
        FileGenUtils.close(failureCloseable)
    }

    @Test(expected = Exception::class)
    fun `close does not catch generic exceptions`() {
        val failureCloseable = Closeable { throw Exception("test") }
        FileGenUtils.close(failureCloseable)
    }
}

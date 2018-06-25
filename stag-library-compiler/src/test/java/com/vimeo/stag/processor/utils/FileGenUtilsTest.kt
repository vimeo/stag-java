package com.vimeo.stag.processor.utils

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.vimeo.stag.processor.Utils
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
    fun `close calls close on closeable once`() {
        val closeable = mock<Closeable>()
        FileGenUtils.close(closeable)

        verify(closeable).close()
        verifyNoMoreInteractions(closeable)
    }

    @Test
    fun `close accepts null closeable`() {
        FileGenUtils.close(null)
    }

    @Test
    fun `close safely catches IO exceptions`() {
        val failureCloseable = Closeable { throw IOException("test") }
        FileGenUtils.close(failureCloseable)
    }

    @Test(expected = Exception::class)
    fun `close does not catch generic exceptions`() {
        val failureCloseable = Closeable { throw Exception("test") }
        FileGenUtils.close(failureCloseable)
    }
}

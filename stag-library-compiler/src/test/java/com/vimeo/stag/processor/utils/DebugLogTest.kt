package com.vimeo.stag.processor.utils

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.verifyNoMoreInteractions
import com.vimeo.stag.processor.Utils
import com.vimeo.stag.processor.utils.logging.DebugLog
import com.vimeo.stag.processor.utils.logging.Logger
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [DebugLog].
 *
 * Created by restainoa on 6/15/18.
 */
class DebugLogTest {

    private val logger = mock<Logger>()

    @Before
    fun setUp() {
        DebugLog.initialize(null)
    }

    @Test
    fun `DebugLog is not instantiable`() {
        Utils.testZeroArgumentConstructorFinalClass(DebugLog::class.java)
    }

    @Test(expected = IllegalStateException::class)
    fun `log without initialization crashes`() {
        DebugLog.log("test")
    }

    @Test(expected = IllegalStateException::class)
    fun `log with tag without initialization crashes`() {
        DebugLog.log("test", "test")
    }

    @Test
    fun `log message works`() {
        DebugLog.initialize(logger)
        DebugLog.log("test message")

        verify(logger).log("Stag: test message")
        verifyNoMoreInteractions(logger)
    }

    @Test
    fun `log with tag and message works`() {
        DebugLog.initialize(logger)
        DebugLog.log("test tag", "test message")

        verify(logger).log("Stag:test tag: test message")
        verifyNoMoreInteractions(logger)
    }
}

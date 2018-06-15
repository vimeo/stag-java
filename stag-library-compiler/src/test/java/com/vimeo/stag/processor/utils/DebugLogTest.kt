package com.vimeo.stag.processor.utils

import com.vimeo.stag.processor.StagProcessor
import com.vimeo.stag.processor.Utils
import org.junit.Test

/**
 * Unit tests for [DebugLog].
 *
 * Created by restainoa on 6/15/18.
 */
class DebugLogTest {

    @Test
    fun `DebugLog is not instantiable`() {
        Utils.testZeroArgumentConstructorFinalClass(DebugLog::class.java)
    }

    @Test
    fun `log message works`() {
        StagProcessor.DEBUG = true
        DebugLog.log("test message")
    }

    @Test
    fun `log with tag and message works`() {
        StagProcessor.DEBUG = true
        DebugLog.log("test tag", "test message")
    }
}

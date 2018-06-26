package com.vimeo.stag.processor.utils.logging

import org.assertj.core.api.Assertions
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Unit tests for [NoOpLogger].
 */
class NoOpLoggerTest {

    @Test
    fun `log does not print to System out`() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteArrayOutputStream))

        val noOpLogger = NoOpLogger()
        noOpLogger.log("test")

        Assertions.assertThat(byteArrayOutputStream.toString()).isEqualTo("")
    }

}

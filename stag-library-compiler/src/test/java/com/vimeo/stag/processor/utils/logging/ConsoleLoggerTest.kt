package com.vimeo.stag.processor.utils.logging

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.ByteArrayOutputStream
import java.io.PrintStream

/**
 * Unit tests for [ConsoleLogger].
 */
class ConsoleLoggerTest {

    @Test
    fun `log prints to System out`() {
        val byteArrayOutputStream = ByteArrayOutputStream()
        System.setOut(PrintStream(byteArrayOutputStream))

        val consoleLogger = ConsoleLogger()
        consoleLogger.log("test")

        assertThat(byteArrayOutputStream.toString()).isEqualTo("test\n")
    }
}

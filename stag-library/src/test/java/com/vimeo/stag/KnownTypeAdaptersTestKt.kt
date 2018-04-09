package com.vimeo.stag

import com.google.gson.JsonSyntaxException
import com.google.gson.stream.JsonReader
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.io.StringReader

/**
 * Created by restainoa on 4/9/18.
 */
class KnownTypeAdaptersTestKt {

    /**
     * Wraps a [String] in quotations.
     */
    private fun String.quote(): String = "\"$this\""

    @Test
    fun `KnownTypeAdapters is not instantiable`() {
        testZeroArgumentConstructorFinalClass(KnownTypeAdapters::class.java)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `Byte TypeAdapter throws JsonSyntaxException if used on non integer`() {
        KnownTypeAdapters.BYTE.fromJson("test".quote())
    }

    @Test
    fun `PrimitiveByteTypeAdapter returns default value for null`() {
        assertThat(KnownTypeAdapters.PrimitiveByteTypeAdapter.read(JsonReader(StringReader("null")), 1)).isEqualTo(1)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `PrimitiveByteTypeAdapter throws JsonSyntaxException if used on non integer`() {
        KnownTypeAdapters.PrimitiveByteTypeAdapter.read(JsonReader(StringReader("test".quote())), 1)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `Short TypeAdapter throws JsonSyntaxException if used on non integer`() {
        KnownTypeAdapters.SHORT.fromJson("test".quote())
    }

    @Test
    fun `PrimitiveShortTypeAdapter returns default value for null`() {
        assertThat(KnownTypeAdapters.PrimitiveShortTypeAdapter.read(JsonReader(StringReader("null")), 1)).isEqualTo(1)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `PrimitiveShortTypeAdapter throws JsonSyntaxException if used on non integer`() {
        KnownTypeAdapters.PrimitiveShortTypeAdapter.read(JsonReader(StringReader("test".quote())), 1)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `Integer TypeAdapter throws JsonSyntaxException if used on non integer`() {
        KnownTypeAdapters.INTEGER.fromJson("test".quote())
    }

    @Test
    fun `PrimitiveIntTypeAdapter returns default value for null`() {
        assertThat(KnownTypeAdapters.PrimitiveIntTypeAdapter.read(JsonReader(StringReader("null")), 1)).isEqualTo(1)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `PrimitiveIntTypeAdapter throws JsonSyntaxException if used on non integer`() {
        KnownTypeAdapters.PrimitiveIntTypeAdapter.read(JsonReader(StringReader("test".quote())), 1)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `Long TypeAdapter throws JsonSyntaxException if used on non long`() {
        KnownTypeAdapters.LONG.fromJson("test".quote())
    }

    @Test
    fun `PrimitiveLongTypeAdapter returns default value for null`() {
        assertThat(KnownTypeAdapters.PrimitiveLongTypeAdapter.read(JsonReader(StringReader("null")), 1L)).isEqualTo(1L)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `PrimitiveLongTypeAdapter throws JsonSyntaxException if used on non integer`() {
        KnownTypeAdapters.PrimitiveLongTypeAdapter.read(JsonReader(StringReader("test".quote())), 1L)
    }

    @Test
    fun `PrimitiveDoubleTypeAdapter returns default value for null`() {
        assertThat(KnownTypeAdapters.PrimitiveDoubleTypeAdapter.read(JsonReader(StringReader("null")), 1.0)).isEqualTo(1.0)
    }

    @Test(expected = JsonSyntaxException::class)
    fun `PrimitiveDoubleTypeAdapter throws JsonSyntaxException if used on non double`() {
        KnownTypeAdapters.PrimitiveDoubleTypeAdapter.read(JsonReader(StringReader("test".quote())), 1.0)
    }

    @Test
    fun `PrimitiveCharTypeAdapter returns default value for null`() {
        assertThat(KnownTypeAdapters.PrimitiveCharTypeAdapter.read(JsonReader(StringReader("null")), 'a')).isEqualTo('a')
    }

    @Test(expected = JsonSyntaxException::class)
    fun `PrimitiveCharTypeAdapter throws JsonSyntaxException if used on non double`() {
        KnownTypeAdapters.PrimitiveCharTypeAdapter.read(JsonReader(StringReader("test".quote())), 'a')
    }

    @Test
    fun `PrimitiveBooleanTypeAdapter returns default value for null`() {
        assertThat(KnownTypeAdapters.PrimitiveBooleanTypeAdapter.read(JsonReader(StringReader("null")), true)).isTrue()
    }

    @Test
    fun `PrimitiveBooleanTypeAdapter parses boolean from string`() {
        assertThat(KnownTypeAdapters.PrimitiveBooleanTypeAdapter.read(JsonReader(StringReader("true".quote())), true)).isTrue()
    }
}

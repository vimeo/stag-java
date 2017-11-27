package com.vimeo.stag.processor.functional

import com.vimeo.sample_java_model.*
import com.vimeo.stag.processor.ProcessorTester
import com.vimeo.stag.processor.StagProcessor
import com.vimeo.stag.processor.isSuccessful
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.reflect.KClass

/**
 * Functional tests for the integrations in the `integration-test-java` module
 */
class JavaIntegrationFunctionalTests {

    private val processorTester = ProcessorTester({ StagProcessor() }, "-AstagAssumeHungarianNotation=true")
    private val module = "integration-test-java"

    @Test
    fun `BooleanFields compiles successfully`() {
       assertThatClassCompilationIsSuccessful(BooleanFields::class)
    }

    @Test
    fun `NullFields compiles successfully`() {
       assertThatClassCompilationIsSuccessful(NullFields::class)
    }

    @Test
    fun `NativeJavaModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NativeJavaModel::class)
    }

    @Test
    fun `NativeJavaModelExtension compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NativeJavaModelExtension::class)
    }

    @Test
    fun `NativeJavaModelExtensionWithoutAnnotation compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NativeJavaModelExtensionWithoutAnnotation::class)
    }

    @Test
    fun `PrivateMembers compiles successfully`() {
        assertThatClassCompilationIsSuccessful(PrivateMembers::class)
    }

    @Test
    fun `SwappableParserExampleModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(SwappableParserExampleModel::class)
    }

    private fun <T: Any> assertThatClassCompilationIsSuccessful(kClass: KClass<T>) {
        assertThat(processorTester.compileClassInModule(module, kClass).isSuccessful()).isTrue()
    }

}
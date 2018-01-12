package com.vimeo.stag.processor.functional

import com.vimeo.sample_java_model.*
import com.vimeo.stag.processor.ProcessorTester
import com.vimeo.stag.processor.StagProcessor
import com.vimeo.stag.processor.isSuccessful
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import kotlin.reflect.KClass

/**
 * Functional tests for the integrations in the `integration-test-java` module.
 */
class JavaIntegrationFunctionalTests {

    private val processorTester = ProcessorTester({ StagProcessor() }, "-AstagAssumeHungarianNotation=true")
    private val module = "integration-test-java"

    @Test
    fun `AlternateNameModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(AlternateNameModel::class)
    }

    @Test
    fun `AlternateNameModel1 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(AlternateNameModel1::class)
    }

    @Test
    fun `BaseExternalModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(BaseExternalModel::class)
    }

    @Test
    fun `BooleanFields compiles successfully`() {
        assertThatClassCompilationIsSuccessful(BooleanFields::class)
    }

    @Test
    fun `EnumWithFieldsModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(EnumWithFieldsModel::class)
    }

    @Test
    fun `ExternalAbstractClass compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalAbstractClass::class)
    }

    @Test
    fun `ExternalModel1 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModel1::class)
    }

    @Test
    fun `ExternalModel2 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModel2::class)
    }

    @Test
    fun `ExternalModelGeneric compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModelGeneric::class)
    }

    @Test
    fun `ExternalModelGeneric1 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModelGeneric1::class)
    }

    @Test
    fun `ModelWithNestedInterface compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ModelWithNestedInterface::class)
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
    fun `RawGenericField compiles successfully`() {
        assertThatClassCompilationIsSuccessful(RawGenericField::class)
    }

    @Test
    fun `NullFields compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NullFields::class)
    }

    @Test
    fun `PrivateMembers compiles successfully`() {
        assertThatClassCompilationIsSuccessful(PrivateMembers::class)
    }

    @Test
    fun `PublicFieldsNoHungarian compiles successfully`() {
        val processorTesterWithNoHungarian = ProcessorTester({ StagProcessor() }, "-AstagAssumeHungarianNotation=false")
        assertThat(processorTesterWithNoHungarian.compileClassInModule(module, PublicFieldsNoHungarian::class).isSuccessful()).isTrue()

        val processorTesterWithHungarian = ProcessorTester({ StagProcessor() }, "-AstagAssumeHungarianNotation=true")
        assertThat(processorTesterWithHungarian.compileClassInModule(module, PublicFieldsNoHungarian::class).isSuccessful()).isTrue()
    }

    @Test
    fun `SwappableParserExampleModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(SwappableParserExampleModel::class)
    }

    @Test
    fun `WrapperTypeAdapterModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(WrapperTypeAdapterModel::class)
    }

    private fun <T : Any> assertThatClassCompilationIsSuccessful(kClass: KClass<T>) {
        assertThat(processorTester.compileClassInModule(module, kClass).isSuccessful()).isTrue()
    }

}

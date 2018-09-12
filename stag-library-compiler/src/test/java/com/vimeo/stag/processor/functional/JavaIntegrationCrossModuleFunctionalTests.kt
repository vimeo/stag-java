package com.vimeo.stag.processor.functional

import com.vimeo.sample.model.*
import com.vimeo.sample.model.basic.BasicModel1
import com.vimeo.sample.model.basic.BasicModel2
import com.vimeo.sample.model.json_adapter.JsonAdapterExample
import com.vimeo.sample.model1.Data
import com.vimeo.sample.model1.ParameterizedData
import com.vimeo.stag.processor.ProcessorTester
import com.vimeo.stag.processor.StagProcessor
import com.vimeo.stag.processor.isSuccessful
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.reflect.KClass

/**
 * Functional tests for the integrations in the `integration-test-java-cross-module` module.
 */
class JavaIntegrationCrossModuleFunctionalTests {

    private val processorTester = ProcessorTester({ StagProcessor() }, "-AstagAssumeHungarianNotation=true")
    private val module = "integration-test-java-cross-module"

    @Test
    fun `BasicModel1 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(BasicModel1::class)
    }

    @Test
    fun `BasicModel2 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(BasicModel2::class)
    }

    @Test
    fun `JsonAdapterExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(JsonAdapterExample::class)
    }

    @Test
    fun `AccessModifiers compiles successfully`() {
        assertThatClassCompilationIsSuccessful(AccessModifiers::class)
    }

    @Test
    fun `AnnotationExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(AnnotationExample::class)
    }

    @Test
    fun `ClassWithArrayTypes compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ClassWithArrayTypes::class)
    }

    @Test
    fun `ClassWithNestedInterface compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ClassWithNestedInterface::class)
    }

    @Test
    fun `ConcreteClass compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ConcreteClass::class)
    }

    @Test
    fun `DuplicateName compiles successfully`() {
        assertThatClassCompilationIsSuccessful(DuplicateName::class)
    }

    @Test
    fun `EnumExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(EnumExample::class)
    }

    @Test
    fun `ExternalModelDerivedExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModelDerivedExample::class)
    }

    @Test
    fun `ExternalModelExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModelExample::class)
    }

    @Test
    fun `ExternalModelExample1 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModelExample1::class)
    }

    @Test
    fun `ExternalModelExample2 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModelExample2::class)
    }

    @Test
    fun `ExternalModelExample3 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ExternalModelExample3::class)
    }

    @Test
    fun `FieldOptionAllExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(FieldOptionAllExample::class)
    }

    @Test
    fun `FieldOptionSerializedName compiles successfully`() {
        assertThatClassCompilationIsSuccessful(FieldOptionsSerializedName::class)
    }

    @Test
    fun `FieldOptionSerializedName2 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(FieldOptionsSerializedName2::class)
    }

    @Test
    fun `FieldOptionSerializedName3 compiles successfully`() {
        assertThatClassCompilationIsSuccessful(FieldOptionsSerializedName3::class)
    }

    @Test
    fun `GenericClass compiles successfully`() {
        assertThatClassCompilationIsSuccessful(GenericClass::class)
    }

    @Test
    fun `IdenticalFieldTypes compiles successfully`() {
        assertThatClassCompilationIsSuccessful(IdenticalFieldTypes::class)
    }

    @Test
    fun `KnownTypeAdaptersExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(KnownTypeAdaptersExample::class)
    }

    @Test
    fun `NestedClass compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NestedClass::class)
    }

    @Test
    fun `NestedEnum compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NestedEnum::class)
    }

    @Test
    fun `NestedModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NestedModel::class)
    }

    @Test
    fun `NoUseStagAnnotation compiles successfully`() {
        assertThatClassCompilationIsSuccessful(NoUseStagAnnotation::class)
    }

    @Test
    fun `ObjectExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ObjectExample::class)
    }

    @Test
    fun `OuterClassWithInnerModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(OuterClassWithInnerModel::class)
    }

    @Test
    fun `PrimitiveTypesExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(PrimitiveTypesExample::class)
    }

    @Test
    fun `RecursiveClass compiles successfully`() {
        assertThatClassCompilationIsSuccessful(RecursiveClass::class)
    }

    @Test
    fun `SampleInterface compiles successfully`() {
        assertThatClassCompilationIsSuccessful(SampleInterface::class)
    }

    @Test
    fun `SubClassWithSameVariableName compiles successfully`() {
        assertThatClassCompilationIsSuccessful(SubClassWithSameVariableName::class)
    }

    @Test
    fun `TestExternalExample compiles successfully`() {
        assertThatClassCompilationIsSuccessful(TestExternalExample::class)
    }

    @Test
    fun `Data compiles successfully`() {
        assertThatClassCompilationIsSuccessful(Data::class)
    }

    @Test
    fun `model1_DuplicateName compiles successfully`() {
        assertThatClassCompilationIsSuccessful(com.vimeo.sample.model1.DuplicateName::class)
    }

    @Test
    fun `ParameterizedData compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ParameterizedData::class)
    }

    private fun <T : Any> assertThatClassCompilationIsSuccessful(kClass: KClass<T>) {
        Assertions.assertThat(processorTester.compileClassesInModule(module, kClass).isSuccessful()).isTrue()
    }
}

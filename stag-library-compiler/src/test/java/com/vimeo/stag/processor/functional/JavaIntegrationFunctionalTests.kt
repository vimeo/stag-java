package com.vimeo.stag.processor.functional

import com.google.testing.compile.Compilation
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
    fun `WildcardModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(WildcardModel::class)
    }

    @Test
    fun `DynamicallyTypedModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(DynamicallyTypedModel::class)
    }

    @Test
    fun `DynamicallyTypedWildcard compiles successfully`() {
        assertThatClassCompilationIsSuccessful(DynamicallyTypedWildcard::class)
    }

    @Test
    fun `AbstractDataList compiles successfully`() {
        assertThatClassCompilationIsSuccessful(AbstractDataList::class)
    }

    @Test
    fun `SuperAbstractDataList compiles successfully`() {
        assertThatClassCompilationIsSuccessful(SuperAbstractDataList::class)
    }

    @Test
    fun `ConcreteDataList compiles successfully`() {
        assertThatClassCompilationIsSuccessful(ConcreteDataList::class)
    }

    @Test
    fun `PublicFieldsNoHungarian compiles successfully`() {
        val processorTesterWithNoHungarian = ProcessorTester({ StagProcessor() }, "-AstagAssumeHungarianNotation=false")
        assertThat(processorTesterWithNoHungarian.compileClassesInModule(module, PublicFieldsNoHungarian::class).isSuccessful()).isTrue()

        val processorTesterWithHungarian = ProcessorTester({ StagProcessor() }, "-AstagAssumeHungarianNotation=true")
        assertThat(processorTesterWithHungarian.compileClassesInModule(module, PublicFieldsNoHungarian::class).isSuccessful()).isTrue()
    }

    @Test
    fun `SwappableParserExampleModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(SwappableParserExampleModel::class)
    }

    @Test
    fun `WrapperTypeAdapterModel compiles successfully`() {
        assertThatClassCompilationIsSuccessful(WrapperTypeAdapterModel::class)
    }

    @Test
    fun `Verify that compilation is deterministic`() {
        val classes = arrayOf(
                AlternateNameModel::class,
                AlternateNameModel1::class,
                BaseExternalModel::class,
                BooleanFields::class,
                EnumWithFieldsModel::class,
                ExternalAbstractClass::class,
                ExternalModel1::class,
                ExternalModel2::class,
                ExternalModelGeneric::class,
                ExternalModelGeneric1::class,
                ModelWithNestedInterface::class,
                NativeJavaModel::class,
                NativeJavaModelExtension::class,
                NativeJavaModelExtensionWithoutAnnotation::class,
                RawGenericField::class,
                NullFields::class,
                PrivateMembers::class,
                WildcardModel::class,
                DynamicallyTypedModel::class,
                DynamicallyTypedWildcard::class,
                AbstractDataList::class,
                SuperAbstractDataList::class,
                ConcreteDataList::class
        )

        val compilation1Hash = processorTester.compileClassesInModule(module, *classes).hashOutput()
        val compilation2Hash = processorTester.compileClassesInModule(module, *classes).hashOutput()

        assertThat(compilation1Hash).isEqualTo(compilation2Hash)
    }

    /**
     * Returns the concatenation of the hash of each file generated by the [Compilation].
     */
    private fun Compilation.hashOutput(): String = generatedFiles()
            .joinToString { it.getCharContent(false).hashCode().toString() }

    private fun <T : Any> assertThatClassCompilationIsSuccessful(kClass: KClass<T>) {
        assertThat(processorTester.compileClassesInModule(module, kClass).isSuccessful()).isTrue()
    }

}

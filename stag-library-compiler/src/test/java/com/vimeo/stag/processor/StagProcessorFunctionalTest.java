package com.vimeo.stag.processor;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import javax.annotation.processing.Processor;
import javax.tools.Diagnostic;

public class StagProcessorFunctionalTest extends AbstractAnnotationProcessorTest {

    private StagProcessor processor;

    @Override
    protected Collection<javax.annotation.processing.Processor> getProcessors() {
        processor = new StagProcessor();
        return Collections.<Processor>singletonList(processor);
    }

    /**
     * Ensure that final fields result in compile-time errors to prevent silent omission of fields
     * from generated type adapters.
     */
    @Test
    public void finalFieldsInAnnotatedClassReportsAsAnError() throws Exception {
        assertCompilationReturned(Diagnostic.Kind.ERROR, 8, compileTestCase("bad/FinalFields"));
    }

    /**
     * Ensure that private fields result in compile-time errors to prevent silent omission of fields
     * from generated type adapters.
     */
    @Test
    public void privateFieldsInAnnotatedClassReportsAsAnError() throws Exception {
        assertCompilationReturned(Diagnostic.Kind.ERROR, 8, compileTestCase("bad/PrivateFields"));
    }

}

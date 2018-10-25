package com.vimeo.stag.processor.codegen;

import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.CodeBlock.Builder;

import org.jetbrains.annotations.NotNull;

/**
 * An extension of JavaPoet that makes creating switches easier.
 * <p>
 * Created by restainoa on 2/21/18.
 */
public class SwitchCodeBlockBuilder {

    private final CodeBlock.Builder mCodeBlockBuilder = CodeBlock.builder();
    private boolean mIsCaseIndented;

    /**
     * Begins a switch.
     *
     * @param switchCode the switch code.
     * @param args       the arguments for the switch, optional.
     * @return the builder.
     */
    public SwitchCodeBlockBuilder beginSwitch(@NotNull String switchCode, @NotNull Object... args) {
        mCodeBlockBuilder.beginControlFlow(switchCode, args);
        return this;
    }

    /**
     * Ends the switch.
     *
     * @return the builder.
     */
    public SwitchCodeBlockBuilder endSwitch() {
        mCodeBlockBuilder.endControlFlow();
        return this;
    }

    /**
     * Begins a case, taking care of control characters like the colon.
     *
     * @param caseCode the case to use.
     * @param args     the optional arguments.
     * @return the builder.
     */
    public SwitchCodeBlockBuilder beginCase(@NotNull String caseCode, @NotNull Object... args) {
        if (mIsCaseIndented) {
            mCodeBlockBuilder.unindent();
        }
        mCodeBlockBuilder.add(caseCode + ":\n", args).indent();
        mIsCaseIndented = true;
        return this;
    }

    /**
     * @see CodeBlock.Builder#addStatement(CodeBlock)
     */
    public SwitchCodeBlockBuilder addStatement(@NotNull String caseCode, @NotNull Object... args) {
        mCodeBlockBuilder.addStatement(caseCode, args);
        return this;
    }

    /**
     * @see CodeBlock.Builder#beginControlFlow(String, Object...)
     */
    public SwitchCodeBlockBuilder beginControlFlow(@NotNull String caseCode, @NotNull Object... args) {
        mCodeBlockBuilder.beginControlFlow(caseCode, args);
        return this;
    }

    /**
     * @see Builder#endControlFlow()
     */
    public SwitchCodeBlockBuilder endControlFlow() {
        mCodeBlockBuilder.endControlFlow();
        return this;
    }

    /**
     * Ends the case with a break.
     *
     * @return the builder.
     */
    public SwitchCodeBlockBuilder endCase() {
        mCodeBlockBuilder.addStatement("break").unindent();
        mIsCaseIndented = false;
        return this;
    }

    /**
     * Builds the code block.
     *
     * @return the {@link CodeBlock}.
     */
    public CodeBlock build() {
        return mCodeBlockBuilder.build();
    }


}

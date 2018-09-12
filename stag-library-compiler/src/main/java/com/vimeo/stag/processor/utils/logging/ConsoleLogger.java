package com.vimeo.stag.processor.utils.logging;

import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link Logger} that prints to the console.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public class ConsoleLogger implements Logger {

    @Override
    public void log(@NotNull String message) {
        System.out.println(message);
    }

}

package com.vimeo.stag.processor.utils.logging;

import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link Logger} that does nothing.
 */
public class NoOpLogger implements Logger {

    @Override
    public void log(@NotNull String message) {
    }

}

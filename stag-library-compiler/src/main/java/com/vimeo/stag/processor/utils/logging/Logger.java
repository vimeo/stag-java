package com.vimeo.stag.processor.utils.logging;

import org.jetbrains.annotations.NotNull;

/**
 * An interface defining a logging utility.
 */
public interface Logger {

    /**
     * Log the message to the output.
     *
     * @param message the message to log to the output.
     */
    void log(@NotNull String message);

}

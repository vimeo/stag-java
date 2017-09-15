package com.vimeo.stag.processor.utils;

import org.jetbrains.annotations.NotNull;

/**
 * Utils related to the {@link String} class.
 * <p>
 * Created by restainoa on 9/6/17.
 */
public final class StringUtils {

    private StringUtils() {}

    /**
     * Converts the character at the specified index to lowercase.
     *
     * @param string the string to process.
     * @param index  the index of the character, must be in bounds or else an {@link IndexOutOfBoundsException} will be
     *               thrown.
     * @return the processed string with the new lowercase character.
     */
    @NotNull
    public static String convertCharAtToLowerCase(@NotNull String string, int index) {
        final char[] chars = string.toCharArray();
        final char c = chars[index];

        chars[index] = Character.toLowerCase(c);

        return String.valueOf(chars);
    }

}

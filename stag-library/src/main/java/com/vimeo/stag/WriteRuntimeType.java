package com.vimeo.stag;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Use this annotation for a field, if you want gson to pick up the runtime type, instead of compile time type.
 * This will work only while serializing, not for deserialization.
 *
 * Eg:
 *
 *
 */
@Target({ElementType.FIELD})
public @interface WriteRuntimeType {
}
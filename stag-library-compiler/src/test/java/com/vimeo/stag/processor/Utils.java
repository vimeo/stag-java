package com.vimeo.stag.processor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertTrue;

public class Utils {

    public static <T> void testZeroArgumentConstructorFinalClass(Class<T> clazz) throws Exception {
        boolean exceptionThrown = false;
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            T instance = constructor.newInstance();
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof UnsupportedOperationException) {
                exceptionThrown = true;
            }
        }
        assertTrue(exceptionThrown);
    }

}

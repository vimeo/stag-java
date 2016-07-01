/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Vimeo
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.vimeo.stag.processor;

import com.google.testing.compile.CompilationRule;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for the annotation processor.
 * Run using: {@code ./gradlew :stag-library-compiler:test --continue}
 */
public class ProcessorUnitTest {

    @Rule
    public CompilationRule rule = new CompilationRule();
    private Elements elements;
    private Types types;


    @Before
    public void setup() {
        elements = rule.getElements();
        types = rule.getTypes();
    }

    @Test
    public void isParameterizedType_isCorrect() throws Exception {

        Map<String, List<Object>> testMap = new HashMap<>();
        assertTrue(TypeUtils.isParameterizedType(getTypeMirror(testMap)));

        List<Object> testList = new ArrayList<>();
        assertTrue(TypeUtils.isParameterizedType(getTypeMirror(testList)));

        String testString = "test";
        assertFalse(TypeUtils.isParameterizedType(getTypeMirror(testString)));

        Object testObject = new Object();
        assertFalse(TypeUtils.isParameterizedType(getTypeMirror(testObject)));
    }

    @Test
    public void getOuterClassType_isCorrect() throws Exception {

        HashMap<String, List<Object>> testMap = new HashMap<>();
        assertTrue(HashMap.class.getName().equals(TypeUtils.getOuterClassType(getTypeMirror(testMap))));

        ArrayList<Object> testList = new ArrayList<>();
        assertTrue(ArrayList.class.getName().equals(TypeUtils.getOuterClassType(getTypeMirror(testList))));

        String testString = "test";
        assertTrue(String.class.getName().equals(TypeUtils.getOuterClassType(getTypeMirror(testString))));

        Object testObject = new Object();
        assertTrue(Object.class.getName().equals(TypeUtils.getOuterClassType(getTypeMirror(testObject))));
    }

    private Element getElement(@NotNull Object object) {
        return elements.getTypeElement(object.getClass().getName());
    }

    private TypeMirror getTypeMirror(@NotNull Object object) {
        return getElement(object).asType();
    }
}
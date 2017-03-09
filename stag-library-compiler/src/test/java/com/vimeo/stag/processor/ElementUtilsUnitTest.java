/*
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

import com.vimeo.stag.processor.dummy.DummyClassWithConstructor;
import com.vimeo.stag.processor.dummy.DummyConcreteClass;
import com.vimeo.stag.processor.dummy.DummyGenericClass;
import com.vimeo.stag.processor.dummy.DummyInheritedClass;
import com.vimeo.stag.processor.utils.ElementUtils;
import com.vimeo.stag.processor.utils.TypeUtils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;

public class ElementUtilsUnitTest extends BaseUnitTest {

    @Before
    public void setup() {
        ElementUtils.initialize(elements);
        TypeUtils.initialize(types);
    }

    @Test
    public void testConstructor() throws Exception {
        Utils.testZeroArgumentConstructorFinalClass(ElementUtils.class);
    }

    @Test
    public void testGetTypeFromQualifiedName() throws Exception {
        Assert.assertEquals(Utils.getTypeMirrorFromClass(String.class),
                ElementUtils.getTypeFromQualifiedName(String.class.getName()));
        Assert.assertEquals(Utils.getTypeMirrorFromClass(Object.class),
                ElementUtils.getTypeFromQualifiedName(Object.class.getName()));
        Assert.assertEquals(Utils.getTypeMirrorFromClass(ArrayList.class),
                ElementUtils.getTypeFromQualifiedName(ArrayList.class.getName()));
        Assert.assertEquals(Utils.getTypeMirrorFromClass(DummyConcreteClass.class),
                ElementUtils.getTypeFromQualifiedName(DummyConcreteClass.class.getName()));
        Assert.assertEquals(Utils.getTypeMirrorFromClass(DummyGenericClass.class),
                ElementUtils.getTypeFromQualifiedName(DummyGenericClass.class.getName()));
        Assert.assertEquals(Utils.getTypeMirrorFromClass(DummyInheritedClass.class),
                ElementUtils.getTypeFromQualifiedName(DummyInheritedClass.class.getName()));

        Assert.assertNotEquals(Utils.getTypeMirrorFromClass(DummyConcreteClass.class),
                ElementUtils.getTypeFromQualifiedName(DummyInheritedClass.class.getName()));
        Assert.assertNotEquals(Utils.getTypeMirrorFromClass(DummyGenericClass.class),
                ElementUtils.getTypeFromQualifiedName(DummyConcreteClass.class.getName()));
        Assert.assertNotEquals(Utils.getTypeMirrorFromClass(DummyInheritedClass.class),
                ElementUtils.getTypeFromQualifiedName(DummyGenericClass.class.getName()));
    }

    @Test
    public void testGetPackage() throws Exception {
        Assert.assertEquals(String.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(String.class)));
        Assert.assertEquals(ArrayList.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(ArrayList.class)));
        Assert.assertEquals(Object.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(Object.class)));
        Assert.assertEquals(DummyGenericClass.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyGenericClass.class)));

        Assert.assertEquals(DummyGenericClass.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyConcreteClass.class)));
        Assert.assertEquals(DummyConcreteClass.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyInheritedClass.class)));
        Assert.assertEquals(DummyInheritedClass.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(DummyGenericClass.class)));


        Assert.assertNotEquals(Object.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(ArrayList.class)));
        Assert.assertNotEquals(List.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(String.class)));
        Assert.assertNotEquals(DummyInheritedClass.class.getPackage().getName(),
                ElementUtils.getPackage(Utils.getTypeMirrorFromClass(Object.class)));
    }

    @Test
    public void testGetConstructor() throws Exception {
        ExecutableElement executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(String.class));
        Assert.assertEquals(executableElement.getEnclosingElement().toString(),Utils.getElementFromClass(String.class).toString());
        Assert.assertEquals(executableElement.getParameters().size(), 0);

        executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(DummyClassWithConstructor.class));
        Assert.assertEquals(executableElement.getEnclosingElement().toString(),Utils.getElementFromClass(DummyClassWithConstructor.class).toString());
        Assert.assertEquals(executableElement.getParameters().size(), 1);
        Assert.assertEquals(executableElement.getParameters().get(0).asType().toString(), Utils.getElementFromClass(String.class).toString());

        executableElement = ElementUtils.getFirstConstructor(Utils.getTypeMirrorFromClass(DummyGenericClass.class));
        Assert.assertEquals(executableElement.getEnclosingElement().toString(), Utils.getElementFromClass(DummyGenericClass.class).toString());
        Assert.assertEquals(executableElement.getParameters().size(), 0);

    }

}

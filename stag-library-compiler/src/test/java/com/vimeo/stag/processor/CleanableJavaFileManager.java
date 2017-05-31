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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.tools.FileObject;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;

/**
 * {@link JavaFileManager} implementation which tracks created files and allows them to be
 * systematically deleted.
 */
class CleanableJavaFileManager implements JavaFileManager {

    private final LinkedBlockingQueue<FileObject> writtenFiles = new LinkedBlockingQueue<>();
    private final JavaFileManager delegate;

    CleanableJavaFileManager(JavaFileManager delegate) {
        this.delegate = delegate;
    }

    void purge() throws IOException {
        ArrayList<FileObject> toDelete = new ArrayList<>();
        writtenFiles.drainTo(toDelete);
        toDelete.forEach(FileObject::delete);
    }

    @Override
    public ClassLoader getClassLoader(Location location) {
        return delegate.getClassLoader(location);
    }

    @Override
    public Iterable<JavaFileObject> list(Location location, String s, Set<JavaFileObject.Kind> set, boolean b) throws IOException {
        return delegate.list(location, s, set, b);
    }

    @Override
    public String inferBinaryName(Location location, JavaFileObject javaFileObject) {
        return delegate.inferBinaryName(location, javaFileObject);
    }

    @Override
    public boolean isSameFile(FileObject fileObject, FileObject fileObject1) {
        return delegate.isSameFile(fileObject, fileObject1);
    }

    @Override
    public boolean handleOption(String s, Iterator<String> iterator) {
        return delegate.handleOption(s, iterator);
    }

    @Override
    public boolean hasLocation(Location location) {
        return delegate.hasLocation(location);
    }

    @Override
    public JavaFileObject getJavaFileForInput(Location location, String s, JavaFileObject.Kind kind) throws IOException {
        return delegate.getJavaFileForInput(location, s, kind);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String s, JavaFileObject.Kind kind, FileObject fileObject) throws IOException {
        JavaFileObject javaFileForOutput = delegate.getJavaFileForOutput(location, s, kind, fileObject);
        writtenFiles.add(javaFileForOutput);
        return javaFileForOutput;
    }

    @Override
    public FileObject getFileForInput(Location location, String s, String s1) throws IOException {
        return delegate.getFileForInput(location, s, s1);
    }

    @Override
    public FileObject getFileForOutput(Location location, String s, String s1, FileObject fileObject) throws IOException {
        FileObject fileForOutput = delegate.getFileForOutput(location, s, s1, fileObject);
        writtenFiles.add(fileForOutput);
        return fileForOutput;
    }

    @Override
    public void flush() throws IOException {
        delegate.flush();
    }

    @Override
    public void close() throws IOException {
        delegate.close();
        purge();
    }

    @Override
    public int isSupportedOption(String s) {
        return delegate.isSupportedOption(s);
    }
}

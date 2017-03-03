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
        for (FileObject file : toDelete) {
            file.delete();
        }
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

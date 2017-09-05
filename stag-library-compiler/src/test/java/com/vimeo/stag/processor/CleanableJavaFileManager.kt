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
package com.vimeo.stag.processor

import java.io.IOException
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import javax.tools.FileObject
import javax.tools.JavaFileManager
import javax.tools.JavaFileObject

/**
 * [JavaFileManager] implementation which tracks created files and allows them to be
 * systematically deleted.
 */
internal class CleanableJavaFileManager(private val delegate: JavaFileManager) : JavaFileManager {

    private val writtenFiles = LinkedBlockingQueue<FileObject>()

    @Throws(IOException::class)
    fun purge() {
        val toDelete = ArrayList<FileObject>()
        writtenFiles.drainTo(toDelete)
        for (file in toDelete) {
            file.delete()
        }
    }

    override fun getClassLoader(location: JavaFileManager.Location) =
            delegate.getClassLoader(location)

    @Throws(IOException::class)
    override fun list(location: JavaFileManager.Location, s: String, set: Set<JavaFileObject.Kind>, b: Boolean) =
            delegate.list(location, s, set, b)

    override fun inferBinaryName(location: JavaFileManager.Location, javaFileObject: JavaFileObject) =
            delegate.inferBinaryName(location, javaFileObject)

    override fun isSameFile(fileObject: FileObject, fileObject1: FileObject) =
            delegate.isSameFile(fileObject, fileObject1)

    override fun handleOption(s: String, iterator: Iterator<String>) =
            delegate.handleOption(s, iterator)

    override fun hasLocation(location: JavaFileManager.Location) =
            delegate.hasLocation(location)

    @Throws(IOException::class)
    override fun getJavaFileForInput(location: JavaFileManager.Location, s: String, kind: JavaFileObject.Kind) =
            delegate.getJavaFileForInput(location, s, kind)

    @Throws(IOException::class)
    override fun getJavaFileForOutput(location: JavaFileManager.Location, s: String, kind: JavaFileObject.Kind, fileObject: FileObject): JavaFileObject {
        val javaFileForOutput = delegate.getJavaFileForOutput(location, s, kind, fileObject)
        writtenFiles.add(javaFileForOutput)
        return javaFileForOutput
    }

    @Throws(IOException::class)
    override fun getFileForInput(location: JavaFileManager.Location, s: String, s1: String) =
            delegate.getFileForInput(location, s, s1)

    @Throws(IOException::class)
    override fun getFileForOutput(location: JavaFileManager.Location, s: String, s1: String, fileObject: FileObject): FileObject {
        val fileForOutput = delegate.getFileForOutput(location, s, s1, fileObject)
        writtenFiles.add(fileForOutput)
        return fileForOutput
    }

    @Throws(IOException::class)
    override fun flush() {
        delegate.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        delegate.close()
        purge()
    }

    override fun isSupportedOption(s: String) = delegate.isSupportedOption(s)
}

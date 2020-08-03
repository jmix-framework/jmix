/*
 * Copyright 2020 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.core;

import java.io.InputStream;

/**
 * Interface of a component to store and load files defined by file reference.
 *
 * @param <R> file reference type
 * @param <I> file info type
 */
public interface FileStorage<R, I> {

    /**
     * Returns file reference type.
     */
    Class<R> getReferenceType();

    /**
     * Creates new file reference, which can be used to store a file.
     */
    R createReference(I fileInfo);

    /**
     * Returns file info for the given file reference.
     *
     * @throws IllegalArgumentException if file reference is invalid
     */
    I getFileInfo(R reference);

    /**
     * Saves an InputStream contents into file storage.
     *
     * @param reference   file reference
     * @param inputStream input stream, must be closed in the calling code
     * @return number of bytes saved
     * @throws IllegalArgumentException if arguments are incorrect
     * @throws FileStorageException     if something goes wrong
     */
    long saveStream(R reference, InputStream inputStream);

    /**
     * Return an input stream to load a file contents.
     *
     * @param reference file reference
     * @return input stream, must be closed after use
     * @throws IllegalArgumentException if arguments are incorrect
     * @throws FileStorageException     if something goes wrong
     */
    InputStream openStream(R reference);

    /**
     * Removes a file from the file storage.
     *
     * @param reference file reference
     * @throws IllegalArgumentException if file reference is invalid
     */
    void removeFile(R reference);

    /**
     * Tests whether the file denoted by this file reference exists.
     *
     * @param reference file reference
     * @return true if the file denoted by this file reference exists
     * @throws IllegalArgumentException if file reference is invalid
     */
    boolean fileExists(R reference);
}

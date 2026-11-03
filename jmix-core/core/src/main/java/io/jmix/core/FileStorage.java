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

import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Interface of a component to store and load files defined by file references.
 */
@NullMarked
public interface FileStorage {

    /**
     * Returns the name of this storage, which will be saved in {@link FileRef}s.
     * <p>
     * Each file storage in the application should have a unique name.
     */
    String getStorageName();

    /**
     * Saves an InputStream contents into the file storage.
     *
     * @param fileName    file name
     * @param inputStream input stream, must be closed in the calling code
     * @return file reference
     * @throws IllegalArgumentException if arguments are incorrect
     * @throws FileStorageException     if something goes wrong
     */
    default FileRef saveStream(String fileName, InputStream inputStream) {
        return saveStream(fileName, inputStream, Collections.emptyMap());
    }

    /**
     * Saves an InputStream contents into the file storage using additional parameters.
     *
     * @param fileName    file name
     * @param inputStream input stream, must be closed in the calling code
     * @param parameters additional parameters that can be used in specific storage implementation
     *                   (for example, can be converted to FileRef parameters). Keys starting with
     *                   {@value FileDigest#RESERVED_PARAMETER_PREFIX} are reserved for the framework
     * @return file reference, which may carry framework parameters such as the {@code jmix-sha-256} digest
     * @throws IllegalArgumentException if arguments are incorrect or a parameter key is reserved for the framework
     * @throws FileStorageException     if something goes wrong
     */
    FileRef saveStream(String fileName, InputStream inputStream, Map<String, Object> parameters);

    /**
     * Returns an input stream to load a file contents.
     *
     * @param reference file reference
     * @return input stream, must be closed after use
     * @throws IllegalArgumentException if arguments are incorrect
     * @throws FileStorageException     if something goes wrong
     */
    InputStream openStream(FileRef reference);

    /**
     * Removes a file from the file storage.
     *
     * @param reference file reference
     * @throws IllegalArgumentException if file reference is invalid
     */
    void removeFile(FileRef reference);

    /**
     * Tests whether the file denoted by this file reference exists.
     *
     * @param reference file reference
     * @return true if the file denoted by this file reference exists
     * @throws IllegalArgumentException if file reference is invalid
     */
    boolean fileExists(FileRef reference);

    /**
     * Calculates the actual digest of a file, that is of the bytes {@link #openStream(FileRef)} returns.
     * <p>
     * The default implementation reads the whole file. A storage may override the method if it has a cheaper way.
     *
     * @param reference file reference
     * @param algorithm algorithm name from the IANA registry, only {@link FileDigest#SHA_256} is supported
     * @return digest of the file
     * @throws IllegalArgumentException if arguments are incorrect or the algorithm is not supported
     * @throws FileStorageException     if something goes wrong
     */
    default FileDigest calculateDigest(FileRef reference, String algorithm) {
        checkNotNullArgument(reference);
        checkNotNullArgument(algorithm);
        FileDigest.checkSupported(algorithm);
        try (InputStream inputStream = openStream(reference)) {
            FileDigestInputStream digestInputStream = new FileDigestInputStream(inputStream, algorithm);
            digestInputStream.transferTo(OutputStream.nullOutputStream());
            return digestInputStream.getDigest();
        } catch (IOException e) {
            throw new FileStorageException(FileStorageException.Type.IO_EXCEPTION, reference.toString(), e);
        }
    }
}

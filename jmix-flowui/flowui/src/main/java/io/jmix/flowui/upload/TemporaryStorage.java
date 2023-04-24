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
package io.jmix.flowui.upload;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;
import io.jmix.core.FileStorageLocator;

import javax.annotation.Nullable;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

/**
 * API for uploading files to the temporary storage.
 */
public interface TemporaryStorage {

    /**
     * Listener to be notified about the progress of uploading file into the temporary storage.
     */
    interface UploadProgressListener {
        /**
         * @param fileId        temporary file ID
         * @param receivedBytes current uploaded bytes count
         */
        void progressChanged(UUID fileId, int receivedBytes);
    }

    /**
     * Store the byte array in a new temporary file.
     *
     * @param data file contents
     * @return temporary file ID. This ID is cached in memory and can be used for subsequent operations.
     */
    UUID saveFile(byte[] data);

    /**
     * Store the content of stream in a new temporary file.
     *
     * @param stream   stream which content is to be stored
     * @param listener optional listener to be notified about storing progress
     * @return temporary file ID. This ID is cached in memory and can be used for subsequent operations.
     */
    UUID saveFile(InputStream stream, @Nullable UploadProgressListener listener);

    /**
     * Create a new empty temporary file and cache its ID for subsequent operations.
     *
     * @return the new temporary file ID
     * @throws FileStorageException in case of IO problems
     */
    FileInfo createFile();

    /**
     * Return a previously registered temporary file by its ID.
     *
     * @param fileId temporary file ID
     * @return temporary file object or null if no file registered under this ID
     */
    @Nullable
    File getFile(UUID fileId);

    /**
     * Remove a file from the temporary storage.
     * <br>
     * This method is automatically called from putFileIntoStorage() when the file is successfully stored on the
     * middleware.
     *
     * @param fileId temporary file ID
     * @throws FileStorageException in case of IO problems
     */
    void deleteFile(UUID fileId);

    /**
     * Remove an entry from the list of currently cached temporary file IDs, if such exists.
     * This method is used by the framework when cleaning up the temp folder.
     *
     * @param fileName absolute path to the temporary file
     */
    void deleteFileLink(String fileName);

    /**
     * Uploads a file from the temporary storage to the FileStorage.
     *
     * @param fileId      temporary file ID
     * @param fileName    file name
     * @param fileStorage file storage
     */
    FileRef putFileIntoStorage(UUID fileId, String fileName, FileStorage fileStorage);

    /**
     * Uploads a file from the temporary storage to the default FileStorage,
     * which is determined by calling {@link FileStorageLocator#getDefault()}.
     *
     * @param fileId   temporary file ID
     * @param fileName file name
     */
    FileRef putFileIntoStorage(UUID fileId, String fileName);

    class FileInfo {
        private UUID id;
        private File file;

        public FileInfo(File file, UUID id) {
            this.file = file;
            this.id = id;
        }

        public File getFile() {
            return file;
        }

        public UUID getId() {
            return id;
        }
    }
}
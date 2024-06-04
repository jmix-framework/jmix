/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui.download;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;

import java.io.InputStream;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * Data provider for FileDescriptor
 */
public class FileRefDownloadDataProvider implements DownloadDataProvider {

    protected FileRef fileReference;
    protected FileStorage fileStorage;

    public FileRefDownloadDataProvider(FileRef fileReference, FileStorage fileStorage) {
        checkNotNullArgument(fileReference, "Null file reference");
        checkNotNullArgument(fileStorage, "Null file storage");

        this.fileReference = fileReference;
        this.fileStorage = fileStorage;
    }

    @Override
    public InputStream getStream() {
        if (!fileStorage.fileExists(fileReference)) {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND, fileReference.toString());
        }
        return fileStorage.openStream(fileReference);
    }
}

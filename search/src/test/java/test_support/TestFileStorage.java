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

package test_support;

import com.google.common.io.ByteStreams;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;
import io.jmix.core.FileStorageException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TestFileStorage implements FileStorage {

    private Map<FileRef, byte[]> files = new HashMap<>();
    private String storageName;

    public TestFileStorage() {
        this.storageName = "testFileStorage";
    }

    public TestFileStorage(String storageName) {
        this.storageName = storageName;
    }

    @Override
    public String getStorageName() {
        return storageName;
    }

    @Override
    public FileRef saveStream(String fileName, InputStream inputStream) {
        byte[] bytes;
        try {
            bytes = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save stream", e);
        }
        FileRef reference = new FileRef(getStorageName(), fileName, fileName);
        files.put(reference, bytes);
        return reference;
    }

    @Override
    public InputStream openStream(FileRef reference) {
        if (!fileExists(reference)) {
            throw new FileStorageException(FileStorageException.Type.FILE_NOT_FOUND, "File not found");
        }
        byte[] bytes = files.get(reference);
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void removeFile(FileRef reference) {
        files.remove(reference);
    }

    @Override
    public boolean fileExists(FileRef reference) {
        return files.containsKey(reference);
    }
}

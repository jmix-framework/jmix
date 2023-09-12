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

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class TestFileStorage implements FileStorage {

    private Map<FileRef, byte[]> files = new HashMap<>();

    @Override
    public String getStorageName() {
        return "testFileStorage";
    }

    @Override
    public FileRef saveStream(String fileName, InputStream inputStream, Map<String, Object> parameters) {
        byte[] bytes;
        try {
            bytes = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> fileRefParams = Maps.toMap(parameters.keySet(), key -> parameters.get(key).toString());
        FileRef fileRef = new FileRef(getStorageName(), fileName, fileName, fileRefParams);
        files.put(fileRef, bytes);
        return fileRef;
    }

    @Override
    public InputStream openStream(FileRef reference) {
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

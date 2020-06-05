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

package test_support.app;

import com.google.common.io.ByteStreams;
import io.jmix.core.FileStorage;
import io.jmix.core.UuidProvider;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class TestFileStorage implements FileStorage<URI, String> {

    private Map<URI, byte[]> files = new HashMap<>();

    @Override
    public Class<URI> getReferenceType() {
        return URI.class;
    }

    @Override
    public URI createReference(String filename) {
        try {
            return new URI("test:" + UuidProvider.createUuid() + ";" + filename);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getFileInfo(URI reference) {
        String path = reference.getRawPath();
        return path.split(";", -1)[1];
    }

    @Override
    public long saveStream(URI reference, InputStream inputStream) {
        byte[] bytes;
        try {
            bytes = ByteStreams.toByteArray(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        files.put(reference, bytes);
        return bytes.length;
    }

    @Override
    public InputStream openStream(URI reference) {
        byte[] bytes = files.get(reference);
        return new ByteArrayInputStream(bytes);
    }

    @Override
    public void removeFile(URI reference) {
        files.remove(reference);
    }

    @Override
    public boolean fileExists(URI reference) {
        return files.containsKey(reference);
    }
}

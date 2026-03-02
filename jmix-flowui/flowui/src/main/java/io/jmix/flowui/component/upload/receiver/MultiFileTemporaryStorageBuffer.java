/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.component.upload.receiver;

import com.vaadin.flow.component.upload.MultiFileReceiver;
import io.jmix.flowui.upload.TemporaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import org.springframework.lang.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component("flowui_MultiFileTemporaryStorageBuffer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class MultiFileTemporaryStorageBuffer implements MultiFileReceiver {
    private static final Logger log = LoggerFactory.getLogger(MultiFileTemporaryStorageBuffer.class);

    protected TemporaryStorage temporaryStorage;

    protected Map<UUID, TemporaryStorageFileData> files = new HashMap<>();

    public MultiFileTemporaryStorageBuffer(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Nullable
    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {
        TemporaryStorage.FileInfo fileInfo = temporaryStorage.createFile();

        OutputStream outputBuffer = createFileOutputStream(fileInfo.getFile());
        files.put(fileInfo.getId(), new TemporaryStorageFileData(fileName, mimeType, fileInfo));

        return outputBuffer;
    }

    /**
     * @param fileId the UUID of the file
     * @return temporary file data for filename or null if not found
     */
    public TemporaryStorageFileData getFileData(UUID fileId) {
        return files.get(fileId);
    }

    /**
     * @return map of (UUID - id of the file, TemporaryStorageFileData - file data)
     * @see TemporaryStorageFileData
     */
    public Map<UUID, TemporaryStorageFileData> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    @Nullable
    protected OutputStream createFileOutputStream(File file) {
        try {
            return new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            log.error("Failed to create temporary file output stream", e);
            return null;
        }
    }
}

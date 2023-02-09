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

package io.jmix.flowui.component.upload.receiver;

import com.vaadin.flow.component.upload.Receiver;
import io.jmix.flowui.upload.TemporaryStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Component("flowui_FileTemporaryStorageBuffer")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class FileTemporaryStorageBuffer implements Receiver {
    private static final Logger log = LoggerFactory.getLogger(FileTemporaryStorageBuffer.class);

    protected TemporaryStorage temporaryStorage;

    protected TemporaryStorageFileData fileData;

    public FileTemporaryStorageBuffer(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Nullable
    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {
        TemporaryStorage.FileInfo fileInfo = temporaryStorage.createFile();

        OutputStream outputBuffer = createFileOutputStream(fileInfo.getFile());
        fileData = new TemporaryStorageFileData(fileName, mimeType, fileInfo);

        return outputBuffer;
    }

    public TemporaryStorageFileData getFileData() {
        return fileData;
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

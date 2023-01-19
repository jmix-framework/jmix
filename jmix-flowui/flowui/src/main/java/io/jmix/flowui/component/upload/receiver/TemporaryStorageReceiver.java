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

@Component("flowui_TemporaryStorageReceiver")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TemporaryStorageReceiver implements Receiver {
    private static final Logger log = LoggerFactory.getLogger(TemporaryStorageReceiver.class);

    protected TemporaryStorage temporaryStorage;

    protected TemporaryStorage.FileInfo fileInfo;
    protected String fileName;
    protected String mimeType;

    public TemporaryStorageReceiver(TemporaryStorage temporaryStorage) {
        this.temporaryStorage = temporaryStorage;
    }

    @Nullable
    @Override
    public OutputStream receiveUpload(String fileName, String mimeType) {
        fileInfo = temporaryStorage.createFile();
        this.fileName = fileName;
        this.mimeType = mimeType;

        return createFileOutputStream(fileInfo.getFile());
    }

    /**
     * @return the file info from temporary storage
     */
    public TemporaryStorage.FileInfo getFileInfo() {
        return fileInfo;
    }

    /**
     * @return name of uploaded file
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return MIME type of uploaded file
     */
    public String getMimeType() {
        return mimeType;
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

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

import io.jmix.flowui.upload.TemporaryStorage;

public class TemporaryStorageFileData {

    protected String fileName;
    protected String mimeType;
    protected TemporaryStorage.FileInfo fileInfo;

    /**
     * Create a FileData instance for a file.
     *
     * @param fileName     the file name
     * @param mimeType     the file MIME type
     * @param fileInfo     the file info from temporary storage
     */
    public TemporaryStorageFileData(String fileName, String mimeType, TemporaryStorage.FileInfo fileInfo) {
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.fileInfo = fileInfo;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getFileName() {
        return fileName;
    }

    public TemporaryStorage.FileInfo getFileInfo() {
        return fileInfo;
    }
}

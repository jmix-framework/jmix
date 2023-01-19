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

package io.jmix.flowui.kit.component.upload;

import com.google.common.base.Strings;

public class JmixFileStorageUploadField<C extends AbstractSingleUploadField<C, V>, V>
        extends AbstractSingleUploadField<C, V> {

    protected FileStoragePutMode fileStoragePutMode = FileStoragePutMode.IMMEDIATE;
    protected String fileStorageName;

    public JmixFileStorageUploadField() {
        super(null);
    }

    public JmixFileStorageUploadField(V defaultValue) {
        super(defaultValue);
    }

    /**
     * @return mode which determines when file will be put into FileStorage
     */
    public FileStoragePutMode getFileStoragePutMode() {
        return fileStoragePutMode;
    }

    /**
     * Sets mode which determines when file will be put into FileStorage.
     */
    public void setFileStoragePutMode(FileStoragePutMode putMode) {
        this.fileStoragePutMode = putMode;
    }

    /**
     * @return the name of FileStorage where the upload file will be placed
     */
    public String getFileStorageName() {
        return fileStorageName;
    }

    /**
     * Sets the name of FileStorage where the upload file will be placed. If not set, the default
     * FileStorage will be used.
     *
     * @param fileStorageName the name of file storage
     */
    public void setFileStorageName(String fileStorageName) {
        this.fileStorageName = fileStorageName;
    }

    @Override
    protected String generateFileName() {
        if (getValue() == null) {
            return Strings.isNullOrEmpty(getFileNotSelectedText())
                    ? FILE_NOT_SELECTED
                    : getFileNotSelectedText();
        }
        return getValue().toString();
    }

    @Override
    protected String getDefaultUploadText() {
        return UPLOAD;
    }
}

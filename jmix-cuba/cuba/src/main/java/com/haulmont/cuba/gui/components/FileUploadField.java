/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.core.entity.FileDescriptor;
import com.haulmont.cuba.gui.data.Datasource;
import io.jmix.core.common.event.Subscription;
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.component.SingleFileUploadField;
import io.jmix.ui.upload.TemporaryStorage;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Component compatible with {@link Datasource}.
 *
 * @deprecated Use {@link io.jmix.ui.component.FileStorageUploadField} instead
 */
@Deprecated
public interface FileUploadField extends UploadField, SingleFileUploadField, Field<FileDescriptor> {

    String NAME = "upload";

    /**
     * Get id of the uploaded file in {@link com.haulmont.cuba.gui.upload.FileUploading}.
     *
     * @return File Id.
     */
    @Nullable
    UUID getFileId();

    /**
     * @return caption to be shown in the file download link next to upload button
     */
    @Nullable
    String getFileName();

    /**
     * Returns FileDescriptor instance of uploaded file. Can be null.
     *
     * @return FileDescriptor instance or null
     */
    @Nullable
    FileDescriptor getFileDescriptor();

    /**
     * Gets content bytes for uploaded file.
     *
     * @return Bytes for uploaded file.
     * @deprecated Please use {@link #getFileId()} method and {@link TemporaryStorage}.
     */
    @Deprecated
    byte[] getBytes();

    /**
     * Sets mode which determines when file will be put into FileStorage.
     */
    void setMode(FileStorageUploadField.FileStoragePutMode mode);

    /**
     * @return mode which determines when file will be put into FileStorage.
     */
    FileStorageUploadField.FileStoragePutMode getMode();

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeFileUploadSucceedListener(Consumer<FileUploadSucceedEvent> listener);

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeBeforeValueClearListener(Consumer<BeforeValueClearEvent> listener);

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeAfterValueClearListener(Consumer<AfterValueClearEvent> listener);

    /**
     * @deprecated Use {@link Supplier} of {@link InputStream} instead.
     */
    @Deprecated
    interface FileContentProvider extends Supplier<InputStream> {
        @Override
        default InputStream get() {
            return provide();
        }

        InputStream provide();
    }
}

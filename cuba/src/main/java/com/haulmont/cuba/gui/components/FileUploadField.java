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
import io.jmix.ui.component.FileStorageUploadField;
import io.jmix.ui.upload.TemporaryStorage;

import javax.annotation.Nullable;

/**
 * Component compatible with {@link Datasource}.
 *
 * @deprecated Use {@link io.jmix.ui.component.FileStorageUploadField} instead
 */
@Deprecated
public interface FileUploadField extends FileStorageUploadField<FileDescriptor>, Field<FileDescriptor> {

    String NAME = "upload";

    /**
     * Return FileDescriptor instance of uploaded file. Can be null.
     *
     * @return FileDescriptor instance or null
     */
    @Nullable
    FileDescriptor getFileDescriptor();

    /**
     * Get content bytes for uploaded file.
     *
     * @return Bytes for uploaded file.
     * @deprecated Please use {@link FileStorageUploadField#getFileId()} method and {@link TemporaryStorage}.
     */
    @Deprecated
    byte[] getBytes();
}
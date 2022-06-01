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

package io.jmix.ui.component;

import io.jmix.core.FileRef;
import io.jmix.ui.meta.*;
import io.jmix.ui.upload.TemporaryStorage;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Component for uploading files to file storage.
 */
@StudioComponent(xmlElement = "fileStorageUpload",
        category = "Components",
        icon = "io/jmix/ui/icon/component/fileUpload.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/file-storage-upload-field.html",
        canvasBehaviour = CanvasBehaviour.BUTTON,
        canvasText = "Upload",
        canvasTextProperty = "uploadButtonCaption")
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, options = "fileRef"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING, properties = {"dataContainer", "property"})
        })
public interface FileStorageUploadField extends SingleFileUploadField, Field<FileRef> {
    String NAME = "fileStorageUpload";

    /**
     * Defines when file will be placed into FileStorage.
     */
    enum FileStoragePutMode {
        /**
         * User have to put file into FileStorage manually.
         */
        MANUAL,
        /**
         * File will be placed into FileStorage right after upload.
         */
        IMMEDIATE
    }

    /**
     * Gets id for uploaded file in {@link TemporaryStorage}.
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
     * Sets mode which determines when file will be put into FileStorage.
     */
    @StudioProperty(name = "fileStoragePutMode", type = PropertyType.ENUMERATION,
            defaultValue = "IMMEDIATE", required = true)
    void setMode(FileStoragePutMode mode);

    /**
     * @return mode which determines when file will be put into FileStorage.
     */
    FileStoragePutMode getMode();

    /**
     * Sets the name of FileStorage where the upload file will be placed.
     *
     * @param fileStorageName the name of file storage
     */
    @StudioProperty(name = "fileStorage", type = PropertyType.STRING)
    void setFileStorageName(@Nullable String fileStorageName);

    /**
     * @return the name of FileStorage where the upload file will be placed
     */
    @Nullable
    String getFileStorageName();
}

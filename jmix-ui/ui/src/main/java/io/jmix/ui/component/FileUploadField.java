/*
 * Copyright 2019 Haulmont.
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

import io.jmix.ui.meta.*;

import javax.annotation.Nullable;

/**
 * Component for uploading files from client to server.
 */
@StudioComponent(xmlElement = "fileUpload",
        category = "Components",
        icon = "io/jmix/ui/icon/component/fileUpload.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/file-upload-field.html",
        canvasBehaviour = CanvasBehaviour.BUTTON,
        canvasText = "Upload",
        canvasTextProperty = "uploadButtonCaption")
@StudioProperties(
        properties = {
                @StudioProperty(name = "property", type = PropertyType.PROPERTY_PATH_REF, options = "byteArray"),
                @StudioProperty(name = "dataContainer", type = PropertyType.DATACONTAINER_REF)
        },
        groups = {
                @PropertiesGroup(constraint = PropertiesConstraint.ALL_OR_NOTHING, properties = {"dataContainer", "property"})
        })
public interface FileUploadField extends SingleFileUploadField, Field<byte[]> {
    String NAME = "fileUpload";

    /**
     * @return caption to be shown in the file download link next to upload button
     */
    @Nullable
    String getFileName();

    /**
     * Sets caption to be shown in the file download link next to upload button.
     * The file name of the newly uploaded file will rewrite the caption.
     * <p>
     * By default: "attachment (file_size Kb)".
     */
    void setFileName(@Nullable String filename);
}
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

import io.jmix.core.common.event.Subscription;
import io.jmix.ui.meta.*;

import java.util.EventObject;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Component for uploading files from client to server that supports multiple file selection.
 */
@StudioComponent(xmlElement = "fileMultiUpload",
        category = "Components",
        icon = "io/jmix/ui/icon/component/fileMultiUpload.svg",
        documentationURL = "https://docs.jmix.io/jmix/%VERSION%/ui/vcl/components/file-multi-upload-field.html",
        canvasBehaviour = CanvasBehaviour.BUTTON,
        canvasText = "Upload",
        canvasTextProperty = "caption")
public interface FileMultiUploadField extends UploadField {

    String NAME = "fileMultiUpload";

    /**
     * Get uploads map
     *
     * @return Map ( UUID - Id of file in Temporary storage, String - FileName )
     */
    Map<UUID, String> getUploadsMap();

    /**
     * Clear uploads list
     */
    void clearUploads();

    /**
     * Adds queue upload complete listener. It is invoked when all selected files are uploaded to the temporary storage.
     *
     * @param listener a listener to add
     * @return a registration object for removing an event listener
     */
    Subscription addQueueUploadCompleteListener(Consumer<QueueUploadCompleteEvent> listener);

    /**
     * Describes queue upload complete event.
     */
    class QueueUploadCompleteEvent extends EventObject {

        public QueueUploadCompleteEvent(FileMultiUploadField source) {
            super(source);
        }

        @Override
        public FileMultiUploadField getSource() {
            return (FileMultiUploadField) super.getSource();
        }
    }
}
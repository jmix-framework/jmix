/*
 * Copyright 2026 Haulmont.
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

package io.jmix.flowui.component.upload;

import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.Upload;

/**
 * Event fired when a file upload succeeds.
 *
 * @param <V> the type of the file data
 */
public class UploadSucceededEvent<V> extends FinishedEvent {

    protected final V data;

    /**
     * Create an instance of the event.
     *
     * @param source   the source of the file
     * @param data     the received file data
     * @param fileName the received file name
     * @param mimeType the MIME type of the received file
     * @param length   the length of the received file
     */
    public UploadSucceededEvent(Upload source, V data,
                                String fileName, String mimeType, long length) {
        super(source, fileName, mimeType, length);

        this.data = data;
    }

    /**
     * Get the received file data.
     *
     * @return the received file data
     */
    public V getData() {
        return data;
    }
}

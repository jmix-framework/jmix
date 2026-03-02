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

package io.jmix.flowui.kit.component.upload.event;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.upload.FinishedEvent;
import com.vaadin.flow.component.upload.Upload;

/**
 * Event is fired when {@link FinishedEvent} of {@link Upload} is occurred. See
 * {@link Upload#addFinishedListener(ComponentEventListener)} for details.
 *
 * @param <C> type of upload field
 * @see FinishedEvent
 */
public class FileUploadFinishedEvent<C extends Component> extends ComponentEvent<C> {

    protected final long length;
    protected final String mimeType;
    protected final String fileName;

    public FileUploadFinishedEvent(C source, String fileName, String mimeType, long length) {
        super(source, false);
        this.mimeType = mimeType;
        this.fileName = fileName;
        this.length = length;
    }

    /**
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the MIME Type of the file
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the length of the file
     */
    public long getContentLength() {
        return length;
    }
}

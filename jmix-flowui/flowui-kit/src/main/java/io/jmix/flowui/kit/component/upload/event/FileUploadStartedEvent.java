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
import com.vaadin.flow.component.upload.StartedEvent;
import com.vaadin.flow.component.upload.Upload;

/**
 * Event is fired when {@link StartedEvent} of {@link Upload} is occurred. See
 * {@link Upload#addStartedListener(ComponentEventListener)} for details.
 *
 * @param <C> type of upload field
 * @see StartedEvent
 */
public class FileUploadStartedEvent<C extends Component> extends ComponentEvent<C> {

    protected String filename;
    protected String mimeType;
    protected long contentLength;

    public FileUploadStartedEvent(C source, String fileName, String mimeType,
                                  long contentLength) {
        super(source, false);
        this.filename = fileName;
        this.mimeType = mimeType;
        this.contentLength = contentLength;
    }

    /**
     * @return the file name
     */
    public String getFilename() {
        return filename;
    }

    /**
     * @return the MIME type
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @return the length of the file that is being uploaded
     */
    public long getContentLength() {
        return contentLength;
    }
}

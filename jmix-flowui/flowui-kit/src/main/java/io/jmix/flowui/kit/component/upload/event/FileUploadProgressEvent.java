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
import com.vaadin.flow.component.upload.ProgressUpdateEvent;
import com.vaadin.flow.component.upload.Upload;

/**
 * Event is fired when {@link ProgressUpdateEvent} of {@link Upload} is occurred. See
 * {@link Upload#addProgressListener(ComponentEventListener)} for details.
 *
 * @param <C> type of upload field
 * @see ProgressUpdateEvent
 */
public class FileUploadProgressEvent<C extends Component> extends ComponentEvent<C> {

    protected final long readBytes;
    protected final long contentLength;

    public FileUploadProgressEvent(C source, long readBytes, long contentLength) {
        super(source, false);
        this.readBytes = readBytes;
        this.contentLength = contentLength;
    }

    /**
     * @return transferred bytes for the current update
     */
    public long getReadBytes() {
        return readBytes;
    }

    /**
     * @return total file size or {@code -1} if unknown
     */
    public long getContentLength() {
        return contentLength;
    }
}

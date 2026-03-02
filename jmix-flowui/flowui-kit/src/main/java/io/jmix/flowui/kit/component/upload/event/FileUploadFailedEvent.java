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
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.upload.FailedEvent;
import com.vaadin.flow.component.upload.Upload;

/**
 * Event is fired when {@link FailedEvent} of {@link Upload} is occurred. See
 * {@link Upload#addFailedListener(ComponentEventListener)} for details.
 *
 * @param <C> type of upload field
 * @see FailedEvent
 */
public class FileUploadFailedEvent<C extends Component> extends FileUploadFinishedEvent<C> {

    protected Exception reason;

    public FileUploadFailedEvent(C source, String fileName, String mimeType, long length, Exception reason) {
        super(source, fileName, mimeType, length);
        this.reason = reason;
    }

    /**
     * @return the exception that caused the failure, or {@code null} otherwise
     */
    public Exception getReason() {
        return reason;
    }

    /**
     * @return the number of uploaded bytes
     */
    @Override
    public long getContentLength() {
        return super.getContentLength();
    }
}

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
import com.vaadin.flow.component.upload.Receiver;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

/**
 * Event is fired when {@link SucceededEvent} of {@link Upload} is occurred. See
 * {@link Upload#addSucceededListener(ComponentEventListener)} for details.
 *
 * @param <C> type of upload field
 * @see SucceededEvent
 */
public class FileUploadSucceededEvent<C extends Component> extends FileUploadFinishedEvent<C> {

    protected Receiver receiver;

    public FileUploadSucceededEvent(C source, String fileName, String mimeType, long length, Receiver receiver) {
        super(source, fileName, mimeType, length);

        this.receiver = receiver;
    }

    /**
     * @return receiver that contains information about uploaded file
     * @see MemoryBuffer
     */
    public <T extends Receiver> T getReceiver() {
        return (T) receiver;
    }
}

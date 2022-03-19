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

package com.haulmont.cuba.web.gui.components;

import com.haulmont.cuba.gui.components.FileMultiUploadField;
import com.vaadin.ui.Component;
import io.jmix.ui.component.UploadField;
import io.jmix.ui.component.impl.FileMultiUploadFieldImpl;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Deprecated
public class WebFileMultiUploadField extends FileMultiUploadFieldImpl
        implements FileMultiUploadField {

    @Override
    public void removeQueueUploadCompleteListener(Consumer<QueueUploadCompleteEvent> listener) {
        unsubscribe(QueueUploadCompleteEvent.class, listener);
    }

    @Override
    public void removeFileUploadStartListener(Consumer<FileUploadStartEvent> listener) {
        unsubscribe(FileUploadStartEvent.class, listener);
    }

    @Override
    public void removeFileUploadFinishListener(Consumer<FileUploadFinishEvent> listener) {
        unsubscribe(FileUploadFinishEvent.class, listener);
    }

    @Override
    public void removeFileUploadErrorListener(Consumer<FileUploadErrorEvent> listener) {
        unsubscribe(FileUploadErrorEvent.class, listener);
    }

    @Override
    public void setDropZone(@Nullable UploadField.DropZone dropZone) {
        this.dropZone = dropZone;

        if (dropZone == null) {
            component.setDropZone(null);
        } else {
            io.jmix.ui.component.Component target = dropZone.getTarget();
            if (target instanceof com.haulmont.cuba.gui.components.Window.Wrapper) {
                target = ((com.haulmont.cuba.gui.components.Window.Wrapper) target).getWrappedWindow();
            }

            Component vComponent = target.unwrapComposition(Component.class);
            this.component.setDropZone(vComponent);
        }
    }
}

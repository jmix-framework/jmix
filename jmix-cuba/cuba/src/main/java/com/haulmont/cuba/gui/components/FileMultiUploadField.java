/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */
package com.haulmont.cuba.gui.components;

import com.haulmont.cuba.gui.components.compatibility.FileMultiUploadFieldQueueUploadCompleteListener;
import io.jmix.core.common.event.Subscription;

import java.util.function.Consumer;

/**
 * Component for uploading files from client to server that supports multiple file selection.
 *
 * @deprecated Use {@link io.jmix.ui.component.FileMultiUploadField} instead
 */
@Deprecated
public interface FileMultiUploadField extends UploadField, io.jmix.ui.component.FileMultiUploadField {

    String NAME = "multiUpload";

    /**
     * @param listener a listener to remove
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    void removeQueueUploadCompleteListener(Consumer<QueueUploadCompleteEvent> listener);

    /**
     * @deprecated Use {@link #addQueueUploadCompleteListener(Consumer)} instead
     */
    @Deprecated
    default void addQueueUploadCompleteListener(QueueUploadCompleteListener listener) {
        addQueueUploadCompleteListener(new FileMultiUploadFieldQueueUploadCompleteListener(listener));
    }

    /**
     * @deprecated Use {@link Subscription} instead
     */
    @Deprecated
    default void removeQueueUploadCompleteListener(QueueUploadCompleteListener listener) {
        removeQueueUploadCompleteListener(new FileMultiUploadFieldQueueUploadCompleteListener(listener));
    }

    /**
     * @see QueueUploadCompleteEvent
     * @deprecated Use {@link #addQueueUploadCompleteListener(Consumer)} instead.
     */
    @Deprecated
    @FunctionalInterface
    interface QueueUploadCompleteListener {
        void queueUploadComplete();
    }
}
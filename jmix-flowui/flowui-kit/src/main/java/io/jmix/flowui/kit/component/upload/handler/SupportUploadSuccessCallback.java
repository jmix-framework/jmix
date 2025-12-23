/*
 * Copyright 2025 Haulmont.
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

package io.jmix.flowui.kit.component.upload.handler;

import com.vaadin.flow.server.streams.UploadMetadata;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.io.Serializable;

/**
 * Interface to support handling upload success callbacks for uploaded data.
 * <p>
 * Provides a mechanism to set a callback that will be invoked upon the successful
 * upload operation.
 *
 * @param <V> the type of the uploaded data
 */
public interface SupportUploadSuccessCallback<V> {

    void setUploadSuccessCallback(@Nullable UploadSuccessCallback<V> successCallback);

    @FunctionalInterface
    public interface UploadSuccessCallback<V> extends Serializable {

        void complete(UploadContext<V> context) throws IOException;
    }

    class UploadContext<V> {

        protected final UploadMetadata uploadMetadata;
        protected final V data;

        public UploadContext(UploadMetadata uploadMetadata, V data) {
            this.uploadMetadata = uploadMetadata;
            this.data = data;
        }

        public UploadMetadata getUploadMetadata() {
            return uploadMetadata;
        }

        public V getData() {
            return data;
        }
    }
}

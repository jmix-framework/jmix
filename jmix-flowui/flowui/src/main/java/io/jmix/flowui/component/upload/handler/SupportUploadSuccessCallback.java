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

package io.jmix.flowui.component.upload.handler;

import com.vaadin.flow.server.streams.UploadMetadata;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.io.Serializable;

public interface SupportUploadSuccessCallback {

    void setUploadSuccessCallback(@Nullable UploadSuccessCallback successCallback);

    @FunctionalInterface
    public interface UploadSuccessCallback extends Serializable {

        void complete(UploadContext context) throws IOException;
    }

    class UploadContext {

        protected final UploadMetadata uploadMetadata;

        public UploadContext(UploadMetadata uploadMetadata) {
            this.uploadMetadata = uploadMetadata;
        }

        public UploadMetadata getUploadMetadata() {
            return uploadMetadata;
        }
    }
}

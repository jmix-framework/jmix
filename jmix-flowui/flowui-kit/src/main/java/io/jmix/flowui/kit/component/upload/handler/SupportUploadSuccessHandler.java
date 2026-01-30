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
 * Interface for components that support setting an upload success handler.
 *
 * @param <V> the type of the uploaded data
 */
public interface SupportUploadSuccessHandler<V> {

    /**
     * Sets a handler to be invoked upon the successful completion of an upload operation.
     * <p>
     * If {@code null} is passed as the parameter, any previously set handler will be cleared.
     *
     * @param handler the handler to be executed on a successful upload;
     *                may be {@code null} to clear any existing handler
     */
    void setUploadSuccessHandler(@Nullable UploadSuccessHandler<V> handler);

    @FunctionalInterface
    interface UploadSuccessHandler<V> extends Serializable {

        /**
         * Invoked when an upload operation completes successfully.
         *
         * @param context the context containing metadata and data about the successfully uploaded file
         * @throws IOException if an I/O error occurs during the completion process
         */
        void complete(UploadSuccessContext<V> context) throws IOException;
    }

    /**
     * Represents context information about a successful upload operation.
     *
     * @param uploadMetadata metadata about the uploaded file
     * @param data           the uploaded data
     * @param <V>            the type of the uploaded data
     */
    record UploadSuccessContext<V>(UploadMetadata uploadMetadata, V data) {
    }
}

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

package io.jmix.flowui.download;

import com.vaadin.flow.component.Component;
import org.springframework.lang.Nullable;

import java.io.Serializable;

/**
 * Interface for components that support setting a download success handler.
 */
public interface SupportDownloadSuccessHandler {

    /**
     * Sets a handler that is invoked when a download operation completes successfully.
     *
     * @param handler the handler to be invoked on successful download,
     *                or {@code null} to remove the current handler
     */
    void setDownloadSuccessHandler(@Nullable DownloadSuccessHandler handler);

    /**
     * Represents a handler for successful download operations.
     */
    @FunctionalInterface
    interface DownloadSuccessHandler extends Serializable {

        /**
         * Invoked when a download operation completes successfully.
         *
         * @param context context information about the completed download
         */
        void complete(DownloadSuccessContext context);
    }

    /**
     * Represents context information about a successful download operation.
     *
     * @param owningComponent the component that initiated the download operation
     * @param fileName        the name of the downloaded file
     * @param contentType     the MIME content type of the downloaded file
     */
    record DownloadSuccessContext(Component owningComponent,
                                  String fileName,
                                  String contentType) {
    }
}

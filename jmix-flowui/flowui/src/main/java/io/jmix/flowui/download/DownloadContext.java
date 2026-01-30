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

/**
 * Represents the context for a file download operation. This class encapsulates relevant metadata and
 * data provider information to facilitate the downloading of a file.
 *
 * @param dataProvider the provider supplying the data to be downloaded
 * @param fileName     the name of the file to be downloaded
 * @param contentType  the MIME type of the file
 * @param cacheMaxAgeSec the maximum time in seconds the file will be considered relevant for caching purposes
 * @param download     {@code true} if the file should be downloaded, {@code false} if it should be viewed
 */
public record DownloadContext(DownloadDataProvider dataProvider, String fileName, String contentType,
                              int cacheMaxAgeSec, boolean download) {

    /**
     * Returns the maximum time in seconds during which the file will be considered relevant.
     * Makes sense for using the built-in PDF viewer in the Chrome browser.
     *
     * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Cache-Control#response_directives">
     *     Cache-Control HTTP | MDN</a>
     */
    @Override
    public int cacheMaxAgeSec() {
        return cacheMaxAgeSec;
    }
}

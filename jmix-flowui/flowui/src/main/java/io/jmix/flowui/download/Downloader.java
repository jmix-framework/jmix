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

package io.jmix.flowui.download;

import io.jmix.core.FileRef;
import io.jmix.core.FileStorage;

import org.springframework.lang.Nullable;

import java.util.function.Predicate;

/**
 * Generic interface to download data from the system.
 */
public interface Downloader {

    /**
     * Sets a file storage where the files will be downloaded from.
     *
     * @param fileStorage file storage
     */
    @Deprecated(since = "2.3", forRemoval = true)
    void setFileStorage(FileStorage fileStorage);

    /**
     * @return {@code true} if downloader should open a new window with the file content
     */
    boolean isShowNewWindow();

    /**
     * Sets explicit new window option.
     *
     * @param showNewWindow {@code true} if downloader opens new window, otherwise {@code false}
     */
    void setShowNewWindow(boolean showNewWindow);

    /**
     * Sets a predicate that checks if file is allowed to be opened.
     * It takes file extension as an input parameter and returns {@code true} if file is allowed to be opened or
     * {@code false} if file should be downloaded.
     *
     * @param viewFilePredicate predicate
     */
    default void setViewFilePredicate(Predicate<String> viewFilePredicate) {}

    /**
     * Downloads an arbitrary resource defined by a DownloadDataProvider.
     *
     * @param dataProvider resource provider
     * @param resourceName resource name
     * @param format       download format, can be null
     */
    void download(DownloadDataProvider dataProvider, String resourceName, @Nullable DownloadFormat format);

    /**
     * Downloads an arbitrary resource defined by a DownloadDataProvider.
     *
     * @param dataProvider resource provider
     * @param resourceName resource name
     */
    void download(DownloadDataProvider dataProvider, String resourceName);

    /**
     * Downloads a file from the {@link io.jmix.core.FileStorage} retrieved by {@link io.jmix.core.FileStorageLocator}
     * using storage name from FileRef.
     *
     * @param fileReference file reference
     */
    void download(FileRef fileReference);

    /**
     * Downloads a file from the {@link io.jmix.core.FileStorage} retrieved by {@link io.jmix.core.FileStorageLocator}
     * using storage name from FileRef.
     *
     * @param fileReference file reference
     * @param format        download format, can be null
     */
    void download(FileRef fileReference, @Nullable DownloadFormat format);

    /**
     * Downloads passed byte array.
     *
     * @param data         data in the form of byte array
     * @param resourceName resource name
     */
    void download(byte[] data, String resourceName);

    /**
     * Downloads passed byte array.
     *
     * @param data         data in the form of byte array
     * @param resourceName resource name
     * @param format       download format, can be null
     */
    void download(byte[] data, String resourceName, @Nullable DownloadFormat format);
}

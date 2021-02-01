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

package io.jmix.core;

/**
 * Provides access to all registered file storage beans of the application.
 * <p>
 * If exactly one file storage registered in the application, this file storage is considered as the default
 * file storage of the application. If more than one file storage registered, the default file storage
 * should be specified in the {@code jmix.core.defaultFileStorage} application property.
 */
public interface FileStorageLocator {

    /**
     * Returns the file storage with the given name determined by {@link FileStorage#getStorageName()}.
     *
     * @param storageName file storage name
     * @return file storage
     * @throws IllegalArgumentException if no file storage with the given name found
     */
    <T extends FileStorage> T getByName(String storageName);

    /**
     * Returns the default file storage of the application.
     *
     * @return file storage
     * @throws IllegalStateException if no file storage registered in the application
     *                               or there are more than one file storage registered and
     *                               the default file storage is not specified
     *                               in the {@code jmix.core.defaultFileStorage} application property.
     */
    <T extends FileStorage> T getDefault();
}
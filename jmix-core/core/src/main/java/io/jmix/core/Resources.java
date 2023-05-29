/*
 * Copyright 2019 Haulmont.
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

import org.springframework.core.io.ResourceLoader;

import jakarta.annotation.Nullable;
import java.io.InputStream;

/**
 * Central infrastructure interface for loading resources.
 *
 * Searches for a resource according to the following rules:
 * <ul>
 *     <li>If the given location represents an URL, searches for this URL.</li>
 *     <li> If the given location starts from {@code classpath:} prefix, searches for a classpath resource.</li>
 *     <li> If not an URL, try to find a file below the {@code conf} directory using the given location
 *     as relative path. If a file found, uses this file.</li>
 *     <li> Otherwise searches for a classpath resource for the given location.</li>
 * </ul>
 */
public interface Resources extends ResourceLoader {

    /**
     * Searches for a resource according to the rules explained in {@link Resources} and returns the resource as stream
     * if found. The returned stream should be closed after use.
     *
     * @param location resource location
     * @return InputStream or null if the resource is not found
     */
    @Nullable
    InputStream getResourceAsStream(String location);

    /**
     * Searches for a resource according to the rules explained in {@link Resources} and returns the resource as string
     * if found.
     *
     * @param location resource location
     * @return resource content or null if the resource is not found
     */
    @Nullable
    String getResourceAsString(String location);
}
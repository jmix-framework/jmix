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

package io.jmix.flowui.xml.layout.loader.shortcut;

import java.util.Map;
import java.util.function.Function;

/**
 * Provides a mapping between shortcut aliases and their corresponding property values.
 * <p>
 * Implementations of this interface define how shortcut aliases are resolved to actual
 * shortcut combinations by mapping them to properties of a specific configuration class.
 *
 * @param <T> the type of configuration properties class that provides the shortcut values
 */
public interface ShortcutAliasProvider<T> {

    /**
     * Returns a map of shortcut aliases to property accessor functions.
     * <p>
     * The map keys represent shortcut aliases (e.g., "GRID_CREATE_SHORTCUT"), and the values
     * are functions that extract the corresponding shortcut combination from the properties object.
     *
     * @return immutable map of aliases to property accessors
     */
    Map<String, Function<T, String>> getAliases();

    /**
     * Returns the properties instance that contains the actual shortcut values.
     *
     * @return configuration properties object
     */
    T getPropertyClass();
}

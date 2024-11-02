/*
 * Copyright 2024 Haulmont.
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

package io.jmix.messagetemplatesflowui;

import io.jmix.messagetemplatesflowui.kit.component.GrapesJsPlugin;
import org.springframework.lang.Nullable;

/**
 * Registry for {@link GrapesJsPlugin}s.
 */
public interface GrapesJsPluginRegistry {

    /**
     * Gets plugin instance by its unique name.
     *
     * @param name name of the plugin
     * @param <T>  type of the plugin
     * @return plugin instance
     * @throws IllegalArgumentException if no plugin with the passed name found
     */
    <T extends GrapesJsPlugin> T get(String name);

    /**
     * Gets plugin instance by its unique name.
     *
     * @param name name of the plugin
     * @param <T>  type of the plugin
     * @return plugin instance or {@code null} if not found
     */
    @Nullable
    <T extends GrapesJsPlugin> T find(String name);

    /**
     * Registers a plugin instance in the registry.
     *
     * @param plugin plugin to register
     * @param name   unique registration name
     * @param <T>    type of the plugin
     */
    @Nullable
    <T extends GrapesJsPlugin> void register(T plugin, String name);
}

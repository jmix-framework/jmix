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

import io.jmix.core.common.util.Preconditions;
import io.jmix.messagetemplatesflowui.kit.component.GrapesJsPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component("msgtmp_GrapesJsPluginRegistryImpl")
public class GrapesJsPluginRegistryImpl implements GrapesJsPluginRegistry {

    private static final Logger log = LoggerFactory.getLogger(GrapesJsPluginRegistryImpl.class);

    protected Map<String, GrapesJsPlugin> pluginByName = new HashMap<>();

    public GrapesJsPluginRegistryImpl(List<GrapesJsPlugin> plugins) {
        for (GrapesJsPlugin plugin : plugins) {
            register(plugin, plugin.getName());
        }
    }

    @Override
    public <T extends GrapesJsPlugin> T get(String name) {
        T plugin = find(name);
        Preconditions.checkNotNullArgument(plugin, "Plugin '%s' is not found".formatted(name));
        return plugin;
    }

    @Override
    public <T extends GrapesJsPlugin> T find(String name) {
        //noinspection unchecked
        return (T) pluginByName.get(name);
    }

    @Override
    public <T extends GrapesJsPlugin> void register(T plugin, String name) {
        Preconditions.checkNotNullArgument(plugin, "GrapesJsPlugin is null");
        Preconditions.checkNotNullArgument(name, "name is null");

        log.trace("Register GrapesJsPlugin: {}, name: {},", plugin.getClass().getSimpleName(), name);

        pluginByName.put(name, plugin);
    }
}

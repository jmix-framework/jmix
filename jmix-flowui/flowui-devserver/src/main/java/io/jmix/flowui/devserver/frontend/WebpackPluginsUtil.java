/*
 * Copyright 2000-2022 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.jmix.flowui.devserver.frontend;

import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonObject;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for Flow Webpack plugins.
 * <p>
 * Unifies getting the list of available plugins.
 */
class WebpackPluginsUtil {

    private WebpackPluginsUtil() {
    }

    /**
     * Target folder for plugins.
     */
    protected static final String PLUGIN_TARGET = "plugins";

    /**
     * Get names for plugins to install into node_modules.
     *
     * @return names of plugins to install
     */
    protected static List<String> getPlugins() {
        try {
            final JsonObject jsonFile = getJsonFile(
                    "plugins/webpack-plugins.json");
            if (jsonFile == null) {
                LoggerFactory.getLogger("WebpackPlugins").error(
                        "Couldn't locate plugins/webpack-plugins.json, no Webpack plugins for Flow will be installed."
                                + "If webpack build fails validate flow-server jar content.");
                return Collections.emptyList();
            }

            final JsonArray plugins = jsonFile.getArray("plugins");
            List<String> pluginsToInstall = new ArrayList<>(plugins.length());
            for (int i = 0; i < plugins.length(); i++) {
                pluginsToInstall.add(plugins.getString(i));
            }
            return pluginsToInstall;
        } catch (IOException ioe) {
            throw new UncheckedIOException(
                    "Couldn't load webpack-plugins.json file", ioe);
        }
    }

    /**
     * Load and parse the requested Json file.
     *
     * @param jsonFilePath path to json file
     * @return parsed Json for file if found
     * @throws IOException thrown for problems reading file
     */
    protected static JsonObject getJsonFile(String jsonFilePath) throws IOException {
        try (final InputStream inputStream = getResourceAsStream(jsonFilePath)) {
            if (inputStream == null) {
                return null;
            }

            String jsonString;
            try (inputStream) {
                jsonString = FrontendUtils.streamToString(inputStream);
            }

            return Json.parse(jsonString);
        }
    }

    /**
     * Get URL for given resource.
     *
     * @param resource resource to get URL for
     * @return resource URL
     */
    protected static URL getResourceUrl(String resource) {
        // do not use, because Studio ClassLoader doesn't contain resources from this module
        ClassLoader cl = TaskInstallWebpackPlugins.class.getClassLoader();
        return cl.getResource(resource);
    }

    /**
     * Get stream for resource.
     *
     * @param resource resource stream
     * @return input stream for resource
     */
    protected static InputStream getResourceAsStream(String resource) {
        ClassLoader cl = TaskInstallWebpackPlugins.class.getClassLoader();
        return FrontendUtils.getResourceAsStream(resource, cl);
    }
}

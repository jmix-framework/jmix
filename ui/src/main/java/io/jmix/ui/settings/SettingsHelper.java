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

package io.jmix.ui.settings;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.ui.settings.component.ComponentSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

/**
 * Utility class for working with component settings.
 */
public final class SettingsHelper {

    private static final Logger log = LoggerFactory.getLogger(SettingsHelper.class);

    protected final static Gson gson = new Gson();

    private SettingsHelper() {
    }

    /**
     * Creates instance of settings class.
     *
     * @param settingsClass class of component settings
     * @param <T>           any settings class that extends {@link ComponentSettings}
     * @return instance of component settings
     */
    public static <T extends ComponentSettings> T createSettings(Class<T> settingsClass) {
        return createSettings(settingsClass, null);
    }

    /**
     * Creates instance of settings class with provided id.
     *
     * @param settingsClass class of component settings
     * @param id            component id
     * @param <T>           any settings class that extends {@link ComponentSettings}
     * @return instance of component settings
     */
    public static <T extends ComponentSettings> T createSettings(Class<T> settingsClass, @Nullable String id) {
        try {
            T settings = ReflectionHelper.newInstance(settingsClass);
            if (id != null) {
                settings.setId(id);
            }
            return settings;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(String.format("Cannot create settings '%s'", settingsClass), e);
        }
    }

    /**
     * Converts JSON string representation of settings to POJO.
     *
     * @param settings      JSON string representation of settings
     * @param settingsClass POJO class of settings
     * @param <T>           any settings class that extends {@link ComponentSettings}
     * @return instance of component settings
     */
    @Nullable
    public static <T extends ComponentSettings> T toComponentSettings(String settings, Class<T> settingsClass) {
        Preconditions.checkNotNullArgument(settings);
        Preconditions.checkNotNullArgument(settingsClass);

        try {
            return gson.fromJson(settings, settingsClass);
        } catch (JsonSyntaxException e) {
            log.error("Cannot map settings: {} to '{}'", settings, settingsClass, e);
            return null;
        }
    }

    /**
     * Converts settings from POJO to JSON string representation.
     *
     * @param settings component settings
     * @return JSON string representation of component settings
     */
    public static String toSettingsString(ComponentSettings settings) {
        Preconditions.checkNotNullArgument(settings);

        return gson.toJson(settings);
    }
}

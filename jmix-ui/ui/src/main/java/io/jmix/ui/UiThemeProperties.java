/*
 * Copyright 2021 Haulmont.
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

package io.jmix.ui;

import io.jmix.ui.settings.UserSettingService;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Theme configuration properties class.
 */
@ConfigurationProperties(prefix = "jmix.ui.theme")
@ConstructorBinding
public class UiThemeProperties {

    /**
     * Name of the currently used UI theme.
     */
    String name;

    /**
     * List of available theme modes, i.e. color presets.
     */
    List<String> modes;

    /**
     * Name of color preset that has no additional style class name.
     * <p>
     * <strong>Note:</strong> can’t be changed without corresponding changes in styles.
     */
    String defaultMode;

    /**
     * Name of color preset to be used if no other settings are available.
     * <p>
     * Either cookie or user settings obtained from {@link UserSettingService} have precedence over this value.
     */
    String defaultModeToUse;

    /**
     * List of available theme size presets.
     */
    List<String> sizes;

    /**
     * Name of size preset that has no additional style class name.
     * <p>
     * <strong>Note:</strong> can’t be changed without corresponding changes in styles.
     */
    String defaultSize;

    /**
     * Name of size preset to be used if no other settings are available.
     * <p>
     * Either cookie or user settings obtained from {@link UserSettingService} have precedence over this value.
     */
    String defaultSizeToUse;

    public UiThemeProperties(
            @DefaultValue("helium") String name,

            @DefaultValue({"light", "dark"}) List<String> modes,
            @DefaultValue("light") String defaultMode,
            @Nullable String defaultModeToUse,

            @DefaultValue({"small", "medium", "large"}) List<String> sizes,
            @DefaultValue("medium") String defaultSize,
            @Nullable String defaultSizeToUse
    ) {
        this.name = name;

        this.modes = modes;
        this.defaultMode = defaultMode;
        this.defaultModeToUse = defaultModeToUse;

        this.sizes = sizes;
        this.defaultSize = defaultSize;
        this.defaultSizeToUse = defaultSizeToUse;
    }

    /**
     * @see #name
     */
    public String getName() {
        return name;
    }

    /**
     * @see #modes
     */
    public List<String> getModes() {
        return modes;
    }

    /**
     * @see #defaultMode
     */
    public String getDefaultMode() {
        return defaultMode;
    }

    /**
     * @see #defaultModeToUse
     */
    @Nullable
    public String getDefaultModeToUse() {
        return defaultModeToUse;
    }

    /**
     * @see #sizes
     */
    public List<String> getSizes() {
        return sizes;
    }

    /**
     * @see #defaultSize
     */
    public String getDefaultSize() {
        return defaultSize;
    }

    /**
     * @see #defaultSizeToUse
     */
    @Nullable
    public String getDefaultSizeToUse() {
        return defaultSizeToUse;
    }
}

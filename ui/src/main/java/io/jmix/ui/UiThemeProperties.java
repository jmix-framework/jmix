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
 * Helium theme configuration properties class.
 */
@ConfigurationProperties(prefix = "jmix.ui.theme")
@ConstructorBinding
public class UiThemeProperties {

    List<String> modes;
    String defaultMode;
    String defaultModeToUse;

    List<String> sizes;
    String defaultSize;
    String defaultSizeToUse;

    public UiThemeProperties(
            @DefaultValue({"light", "dark"}) List<String> modes,
            @DefaultValue("light") String defaultMode,
            @Nullable String defaultModeToUse,

            @DefaultValue({"small", "medium", "large"}) List<String> sizes,
            @DefaultValue("medium") String defaultSize,
            @Nullable String defaultSizeToUse
    ) {
        this.modes = modes;
        this.defaultMode = defaultMode;
        this.defaultModeToUse = defaultModeToUse;

        this.sizes = sizes;
        this.defaultSize = defaultSize;
        this.defaultSizeToUse = defaultSizeToUse;
    }

    /**
     * @return the list of available theme modes, i.e. color presets
     */
    public List<String> getModes() {
        return modes;
    }

    /**
     * Returns the name of color preset that has no additional style class name.
     * <p>
     * <strong>Note:</strong> can’t be changed without corresponding changes in styles.
     *
     * @return the name of color preset that has no additional style class name
     */
    public String getDefaultMode() {
        return defaultMode;
    }

    /**
     * Returns the name of color preset to be used if no other settings are available.
     * <p>
     * Either cookie or user settings obtained from {@link UserSettingService}
     * have precedence over this value.
     *
     * @return the name of color preset to be used if no other settings are available
     */
    @Nullable
    public String getDefaultModeToUse() {
        return defaultModeToUse;
    }

    /**
     * @return the list of available theme size presets
     */
    public List<String> getSizes() {
        return sizes;
    }

    /**
     * Returns the name of size preset that has no additional style class name.
     * <p>
     * <strong>Note:</strong> can’t be changed without corresponding changes in styles.
     *
     * @return the name of size preset that has no additional style class name
     */
    public String getDefaultSize() {
        return defaultSize;
    }

    /**
     * Returns the name of size preset to be used if no other settings are available.
     * <p>
     * Either cookie or user settings obtained from {@link UserSettingService}
     * have precedence over this value.
     *
     * @return the name of size preset to be used if no other settings are available
     */
    @Nullable
    public String getDefaultSizeToUse() {
        return defaultSizeToUse;
    }
}

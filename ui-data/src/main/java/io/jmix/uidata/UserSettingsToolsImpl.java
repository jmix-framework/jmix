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

package io.jmix.uidata;

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import io.jmix.ui.UiProperties;
import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.Window;
import io.jmix.ui.settings.UserSettingService;
import io.jmix.ui.settings.UserSettingsTools;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.uidata.settings.ScreenSettings;
import io.jmix.uidata.settings.facet.ScreenSettingsFacet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.function.Consumer;

@Internal
public class UserSettingsToolsImpl implements UserSettingsTools {

    @Autowired
    protected UserSettingService userSettingService;

    @Autowired
    protected UiProperties uiProperties;

    @Autowired
    protected ApplicationContext applicationContext;

    public AppWorkArea.Mode loadAppWindowMode() {
        String s = userSettingService.loadSetting("appWindowMode");
        if (s != null) {
            if (AppWorkArea.Mode.SINGLE.name().equals(s)) {
                return AppWorkArea.Mode.SINGLE;
            } else if (AppWorkArea.Mode.TABBED.name().equals(s)) {
                return AppWorkArea.Mode.TABBED;
            }
        }
        return AppWorkArea.Mode.valueOf(uiProperties.getAppWindowMode().toUpperCase());
    }

    public void saveAppWindowMode(AppWorkArea.Mode mode) {
        Preconditions.checkNotNullArgument(mode);

        userSettingService.saveSetting("appWindowMode", mode.name());
    }

    public String loadTheme() {
        String s = userSettingService.loadSetting("theme");
        if (s != null) {
            return s;
        }
        return uiProperties.getTheme();
    }

    public void saveAppWindowTheme(String theme) {
        userSettingService.saveSetting("theme", theme);
    }

    @Override
    public <T extends ComponentSettings> T toComponentSettings(String settings, Class<T> settingsClass) {
        // screen id is empty as we won't commit any changes
        ScreenSettings screenSettings = (ScreenSettings) applicationContext.getBean(ScreenSettings.NAME, "");
        return screenSettings.toComponentSettings(settings, settingsClass);
    }

    @Override
    public String toSettingsString(ComponentSettings settings) {
        // screen id is empty as we won't commit any changes
        ScreenSettings screenSettings = (ScreenSettings) applicationContext.getBean(ScreenSettings.NAME, "");
        return screenSettings.toSettingsString(settings);
    }

    @Override
    public void applyLazyTabSettings(Window window, Component source, ComponentContainer tabContent) {
        Preconditions.checkNotNullArgument(window);
        Preconditions.checkNotNullArgument(tabContent);

        window.getFacets().forEach(facet -> {
            if (facet instanceof ScreenSettingsFacet) {
                ScreenSettingsFacet settingsFacet = (ScreenSettingsFacet) facet;
                Consumer<ScreenSettingsFacet.SettingsContext> applyHandler = settingsFacet.getApplySettingsDelegate();

                ScreenSettings settings = settingsFacet.getSettings();
                if (settings == null) {
                    throw new IllegalStateException("ScreenSettingsFacet is not attached to the frame");
                }

                if (applyHandler != null) {
                    applyHandler.accept(new ScreenSettingsFacet.SettingsContext(
                            source,
                            tabContent.getComponents(),
                            settings));
                } else {
                    settingsFacet.applySettings(settings);
                }
            }
        });
    }
}

/*
 * Copyright (c) 2008-2016 Haulmont.
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
 *
 */

package io.jmix.ui.settings;

import io.jmix.ui.component.Accordion;
import io.jmix.ui.component.AppWorkArea;
import io.jmix.ui.component.Component;
import io.jmix.ui.component.ComponentContainer;
import io.jmix.ui.component.TabSheet;
import io.jmix.ui.component.Window;
import io.jmix.ui.settings.component.ComponentSettings;

import javax.annotation.Nullable;

/**
 * Utility bean for work with user settings on web client tier.
 */
public interface UserSettingsTools {

    String NAME = "ui_UserSettingsTools";

    AppWorkArea.Mode loadAppWindowMode();

    void saveAppWindowMode(AppWorkArea.Mode mode);

    String loadTheme();

    void saveAppWindowTheme(String theme);

    FoldersState loadFoldersState();

    /**
     * Converts the string representation of settings to the given component settings class.
     *
     * @param settings      settings string representation
     * @param settingsClass component settings class
     * @param <T>           type of component settings
     * @return component settings instance
     */
    @Nullable
    <T extends ComponentSettings> T toComponentSettings(String settings, Class<T> settingsClass);

    /**
     * Converts component settings to a string representation.
     *
     * @param settings component settings
     * @return string representation of settings
     */
    String toSettingsString(ComponentSettings settings);

    /**
     * Helps to apply settings for the components in a lazy tab e.g. {@link TabSheet} or {@link Accordion}.
     *
     * @param window     screen window
     * @param source     component source
     * @param tabContent tab content
     */
    void applyLazyTabSettings(Window window, Component source, ComponentContainer tabContent);

//    todo folders panel
//    void saveFoldersState(boolean visible, int horizontalSplit, int verticalSplit);

    class FoldersState {

        public final boolean visible;
        public final int horizontalSplit;
        public final int verticalSplit;

        public FoldersState(boolean visible, int horizontalSplit, int verticalSplit) {
            this.horizontalSplit = horizontalSplit;
            this.verticalSplit = verticalSplit;
            this.visible = visible;
        }
    }
}
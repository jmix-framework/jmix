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

package com.haulmont.cuba.settings;

public interface CubaLegacySettings {

    /**
     * This method is called by the framework after opening the screen to apply user settings to all components.
     */
    void applySettings(Settings settings);

    /**
     * This method is called by the framework when closing the screen to save user settings if they have been changed.
     */
    void saveSettings();

    /**
     * @return object encapsulating user settings for the current screen
     */
    Settings getSettings();

    /**
     * This method is called by the framework on reset to defaults action
     */
    void deleteSettings();

    /**
     * Applies screen settings to data components.
     *
     * @param settings screen settings
     */
    void applyDataLoadingSettings(Settings settings);
}

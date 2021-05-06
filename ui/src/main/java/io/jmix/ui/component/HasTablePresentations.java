/*
 * Copyright 2019 Haulmont.
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

package io.jmix.ui.component;

import io.jmix.ui.presentation.TablePresentations;
import io.jmix.ui.settings.component.ComponentSettings;
import io.jmix.ui.settings.component.SettingsWrapper;

import javax.annotation.Nullable;

/**
 * Component having presentations.
 */
public interface HasTablePresentations {

    void resetPresentation();
    void loadPresentations();

    @Nullable
    TablePresentations getPresentations();

    void applyPresentation(Object id);
    void applyPresentationAsDefault(Object id);

    @Nullable
    Object getDefaultPresentationId();

    /**
     * Sets default settings for a component. When the presentation is reset it will be applied for the component.
     *
     * @param wrapper settings wrapper
     */
    void setDefaultSettings(SettingsWrapper wrapper);

    /**
     * @return default settings for a component or null if not set
     */
    @Nullable
    ComponentSettings getDefaultSettings();
}
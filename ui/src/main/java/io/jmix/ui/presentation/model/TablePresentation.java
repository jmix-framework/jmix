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

package io.jmix.ui.presentation.model;

import io.jmix.core.JmixEntity;
import io.jmix.ui.presentation.TablePresentations;

import javax.annotation.Nullable;

/**
 * Base interface for Presentation entity.
 * <p>
 * <br>
 * Note that by default, UI does not provide persistence functionality for presentations. To save/load presentations add
 * "ui-persistence" add-on.
 *
 * @see TablePresentations
 */
public interface TablePresentation extends JmixEntity {

    @Nullable
    String getName();

    void setName(@Nullable String name);

    @Nullable
    String getSettings();

    void setSettings(@Nullable String settings);

    @Nullable
    String getUserLogin();

    void setUserLogin(@Nullable String userLogin);

    @Nullable
    Boolean getDefault();

    void setDefault(@Nullable Boolean isDefault);

    @Nullable
    Boolean getAutoSave();

    void setAutoSave(@Nullable Boolean autoSave);

    void setComponentId(String componentId);

}

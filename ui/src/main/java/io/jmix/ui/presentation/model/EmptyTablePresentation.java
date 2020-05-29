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

package io.jmix.ui.presentation.model;

/**
 * Stub. By default, UI does not provide persistence functionality for presentations. To save/load presentations add
 * "ui-persistence" add-on.
 */
public class EmptyTablePresentation implements TablePresentation {

    protected String name;

    protected String settings;

    protected String userLogin;

    protected Boolean isDefault;

    protected Boolean autoSave;

    protected String componentId;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getSettings() {
        return settings;
    }

    @Override
    public void setSettings(String settings) {
        this.settings = settings;
    }

    @Override
    public String getUserLogin() {
        return userLogin;
    }

    @Override
    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    @Override
    public Boolean getDefault() {
        return isDefault;
    }

    @Override
    public void setDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public Boolean getAutoSave() {
        return autoSave;
    }

    @Override
    public void setAutoSave(Boolean autoSave) {
        this.autoSave = autoSave;
    }

    @Override
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }
}

/*
 * Copyright 2025 Haulmont.
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

package io.jmix.appsettingsflowui;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "jmix.appsettings.ui")
public class AppSettingsUiProperties {
    /**
     * Defines whether uuid fields are displayed in the Application Settings view
     */
    final boolean showUuidFields;

    public AppSettingsUiProperties(@DefaultValue("true") boolean showUuidFields) {
        this.showUuidFields = showUuidFields;
    }

    /**
     * @see #showUuidFields
     */
    public boolean isShowUuidFields() { return showUuidFields; }
 }

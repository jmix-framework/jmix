/*
 * Copyright 2024 Haulmont.
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

package io.jmix.search.index.impl;

/**
 * The container of the two independent index configuration comparison result parts.
 * The first one contains the result of the mappings' comparison.
 * The second one contains the result of the settings' comparison.
 */
public class ConfigurationComparingResult {

    protected final MappingComparingResult mappingComparingResult;
    protected final SettingsComparingResult settingsComparingResult;

    ConfigurationComparingResult(MappingComparingResult mappingComparingResult, SettingsComparingResult settingsComparingResult) {
        this.mappingComparingResult = mappingComparingResult;
        this.settingsComparingResult = settingsComparingResult;
    }

    public boolean isIndexRecreatingRequired() {
        return mappingComparingResult.isIndexRecreatingRequired() || settingsComparingResult.isIndexRecreatingRequired();
    }

    public boolean isMappingUpdateRequired() {
        return mappingComparingResult.isConfigurationUpdateRequired();
    }

    public boolean isSettingsUpdateRequired() {
        return settingsComparingResult.isConfigurationUpdateRequired();
    }

    public boolean isConfigurationUpdateRequired() {
        return isMappingUpdateRequired() || isSettingsUpdateRequired();
    }
}

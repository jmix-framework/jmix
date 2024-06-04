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

import org.springframework.stereotype.Component;

import java.util.Map;

@Component("search_SearchSettingsComparator")
public class IndexSettingsComparator {
    public SettingsComparingResult compare(Map<String, String> searchServerSettings, Map<String, String> applicationSettings) {

        long unmatchedSettings = applicationSettings.keySet().stream().filter(key -> {
            String actualValue = applicationSettings.get(key);
            String currentValue = searchServerSettings.get(key);
            return !actualValue.equals(currentValue);
        }).count();

        return unmatchedSettings == 0 ? SettingsComparingResult.SETTINGS_ARE_EQUAL : SettingsComparingResult.SETTINGS_ARE_NOT_COMPATIBLE;
    }

    enum SettingsComparingResult implements ConfigurationPartComparingResult{
        SETTINGS_ARE_EQUAL,
        SETTINGS_ARE_NOT_COMPATIBLE;

        @Override
        public boolean indexRecreatingIsRequired() {
            return this == SETTINGS_ARE_NOT_COMPATIBLE;
        }

        @Override
        public boolean configurationUpdateIsRequired() {
            return false;
        }
    }
}

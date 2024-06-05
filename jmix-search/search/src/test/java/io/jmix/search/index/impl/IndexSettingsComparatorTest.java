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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IndexSettingsComparatorTest {

    @Test
    void compare_equal() {
        IndexSettingsComparator indexSettingsComparator = new IndexSettingsComparator();
        Map<String, String> indexSettings = Map.of("setting1", "value1", "setting2", "value2");
        Map<String, String> applicationSetting = Map.of("setting1", "value1", "setting2", "value2");
        IndexSettingsComparator.SettingsComparingResult result = indexSettingsComparator.compare(indexSettings, applicationSetting);
        assertEquals(IndexSettingsComparator.SettingsComparingResult.EQUAL, result);
    }

    @Test
    void compare_equal_more_settings_in_index() {
        IndexSettingsComparator indexSettingsComparator = new IndexSettingsComparator();
        Map<String, String> indexSettings = Map.of("setting1", "value1", "setting2", "value2", "setting3", "value3");
        Map<String, String> applicationSetting = Map.of("setting1", "value1", "setting2", "value2");
        IndexSettingsComparator.SettingsComparingResult result = indexSettingsComparator.compare(indexSettings, applicationSetting);
        assertEquals(IndexSettingsComparator.SettingsComparingResult.EQUAL, result);
    }

    @Test
    void compare_additional_setting_in_application() {
        IndexSettingsComparator indexSettingsComparator = new IndexSettingsComparator();
        Map<String, String> indexSettings = Map.of("setting1", "value1", "setting2", "value2");
        Map<String, String> applicationSetting = Map.of("setting1", "value1", "setting2", "value2", "setting3", "value3");
        IndexSettingsComparator.SettingsComparingResult result = indexSettingsComparator.compare(indexSettings, applicationSetting);
        assertEquals(IndexSettingsComparator.SettingsComparingResult.NOT_COMPATIBLE, result);
    }
}
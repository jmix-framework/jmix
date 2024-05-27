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

import org.elasticsearch.common.settings.Settings;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchSettingsComparatorTest {

    @Test
    void compare_equal() {
        SearchSettingsComparator searchSettingsComparator = new SearchSettingsComparator();
        Settings indexSettings = mockSettings(Map.of("setting1", "value1", "setting2", "value2"));
        Settings applicationSetting = mockSettings(Map.of("setting1", "value1", "setting2", "value2"));
        ComparingState result = searchSettingsComparator.compare(indexSettings, applicationSetting);
        assertEquals(ComparingState.EQUAL, result);
    }

    @Test
    void compare_equal_more_settings_in_index() {
        SearchSettingsComparator searchSettingsComparator = new SearchSettingsComparator();
        Settings indexSettings = mockSettings(Map.of("setting1", "value1", "setting2", "value2", "setting3", "value3"));
        Settings applicationSetting = mockSettings(Map.of("setting1", "value1", "setting2", "value2"));
        ComparingState result = searchSettingsComparator.compare(indexSettings, applicationSetting);
        assertEquals(ComparingState.EQUAL, result);
    }

    @Test
    void compare_additional_setting_in_application() {
        SearchSettingsComparator searchSettingsComparator = new SearchSettingsComparator();
        Settings indexSettings = mockSettings(Map.of("setting1", "value1", "setting2", "value2"));
        Settings applicationSetting = mockSettings(Map.of("setting1", "value1", "setting2", "value2", "setting3", "value3"));
        ComparingState result = searchSettingsComparator.compare(indexSettings, applicationSetting);
        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }


    @Test
    void compare_additional_setting_in_application_dynamic_attribute() {
        SearchSettingsComparator searchSettingsComparator = new SearchSettingsComparator();
        Settings indexSettings = mockSettings(Map.of("setting1", "value1", "setting2", "value2"));
        Settings applicationSetting = mockSettings(Map.of("setting1", "value1", "setting2", "value2", "index.number_of_replicas", "value3"));
        ComparingState result = searchSettingsComparator.compare(indexSettings, applicationSetting);
        assertEquals(ComparingState.NOT_COMPATIBLE, result);
    }

    private static Settings mockSettings(Map<String, Object> settingsMap) {
        Settings mock = mock(Settings.class);
        when(mock.keySet()).thenReturn(settingsMap.keySet());
        when(mock.get(anyString())).thenAnswer(invocation -> settingsMap.get((String) invocation.getArgument(0)));
        return mock;
    }
}
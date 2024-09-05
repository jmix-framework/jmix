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

import io.jmix.search.index.IndexConfiguration;
import jakarta.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IndexConfigurationComparatorTest {

    @Test
    void compareConfigurations() {
        IndexConfiguration configurationMock = mock(IndexConfiguration.class);
        IndexConfigurationComparator<?, ?, ?> comparator = mock(IndexConfigurationComparator.class, Answers.CALLS_REAL_METHODS);
        when(comparator.getIndexState(configurationMock)).thenReturn(null);
        IndexConfigurationComparator.ConfigurationComparingResult result = comparator.compareConfigurations(configurationMock);
        assertEquals(result.mappingComparingResult, MappingComparingResult.NOT_COMPATIBLE);
        assertEquals(result.settingsComparingResult, SettingsComparingResult.NOT_COMPATIBLE);
    }
}
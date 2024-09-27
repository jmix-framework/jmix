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

package io.jmix.search.index.impl

import io.jmix.search.index.IndexConfiguration
import spock.lang.Specification

class IndexConfigurationComparatorTest extends Specification {

    def "IsIndexRecreatingRequired returns NOT_COMPATIBLE if the resolved index state is null"() {
        given:
        IndexConfiguration configurationMock = Mock(IndexConfiguration.class)
        IndexConfigurationComparator<?, ?, ?> comparator
                = new IndexConfigurationComparatorTestImpl(null, null, null)
        comparator.getIndexState(configurationMock) >> null

        when:
        ConfigurationComparingResult result = comparator.compareConfigurations(configurationMock)

        then:
        result.mappingComparingResult == MappingComparingResult.NOT_COMPATIBLE
        result.settingsComparingResult == SettingsComparingResult.NOT_COMPATIBLE
    }

    private static class IndexConfigurationComparatorTestImpl extends IndexConfigurationComparator {

        IndexConfigurationComparatorTestImpl(IndexMappingComparator mappingComparator, IndexSettingsComparator settingsComparator, IndexStateResolver indexStateResolver) {
            super(mappingComparator, settingsComparator, indexStateResolver)
        }

        @Override
        protected Object getIndexState(IndexConfiguration indexConfiguration) {
            return null
        }
    }
}
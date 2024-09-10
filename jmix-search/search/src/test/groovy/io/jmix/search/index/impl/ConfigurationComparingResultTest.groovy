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

import spock.lang.Specification

class ConfigurationComparingResultTest extends Specification {
    def "IsIndexRecreatingRequired"() {
        when:
        def result = new ConfigurationComparingResult(mappingComparingResult, settingsComparingResult)

        then:
        result.isConfigurationUpdateRequired() == isConfigurationUpdateRequired
        result.isIndexRecreatingRequired() == isIndexRecreatingRequired
        result.isMappingUpdateRequired() == isMappingUpdateRequired
        result.isSettingsUpdateRequired() == isSettingsUpdateRequired

        where:
        mappingComparingResult                | settingsComparingResult                | isConfigurationUpdateRequired | isIndexRecreatingRequired | isMappingUpdateRequired | isSettingsUpdateRequired
        MappingComparingResult.EQUAL          | SettingsComparingResult.EQUAL          | false                         | false                     | false                   | false
        MappingComparingResult.EQUAL          | SettingsComparingResult.NOT_COMPATIBLE | false                         | true                      | false                   | false
        MappingComparingResult.UPDATABLE      | SettingsComparingResult.EQUAL          | true                          | false                     | true                    | false
        MappingComparingResult.UPDATABLE      | SettingsComparingResult.NOT_COMPATIBLE | true                          | true                      | true                    | false
        MappingComparingResult.NOT_COMPATIBLE | SettingsComparingResult.EQUAL          | false                         | true                      | false                   | false
        MappingComparingResult.NOT_COMPATIBLE | SettingsComparingResult.NOT_COMPATIBLE | false                         | true                      | false                   | false

    }
}

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

package io.jmix.search.index

import io.jmix.search.index.mapping.IndexConfigurationManager
import spock.lang.Specification

class EntityIndexingManagementFacadeTest extends Specification {
    def "RefreshLocalIndexConfigurations"() {
        given:
        IndexConfiguration configuration1 = Mock()
        configuration1.getEntityName() >> "FirstEntityName"
        configuration1.getIndexName() >> "FirstIndexName"

        and:
        IndexConfiguration configuration2 = Mock()
        configuration2.getEntityName() >> "SecondEntityName"
        configuration2.getIndexName() >> "SecondIndexName"

        and:
        IndexConfigurationManager configurationManager = Mock()
        configurationManager.refreshIndexDefinitions() >> [configuration1, configuration2]
        and:

        def facade = new EntityIndexingManagementFacade()
        facade.indexConfigurationManager = configurationManager

        when:
        def result = facade.refreshLocalIndexConfigurations()

        then:
        result == "Previous configurations have been deleted. Following 2 configurations have been created:" +
                "\r\n\tEntity=FirstEntityName, Index=FirstIndexName" +
                "\r\n\tEntity=SecondEntityName, Index=SecondIndexName"
    }

    def "RefreshLocalIndexConfigurations. Empty result"() {
        given:
        IndexConfigurationManager configurationManager = Mock()
        configurationManager.refreshIndexDefinitions() >> []
        and:

        def facade = new EntityIndexingManagementFacade()
        facade.indexConfigurationManager = configurationManager

        when:
        def result = facade.refreshLocalIndexConfigurations()

        then:
        result == "Previous configurations have been deleted. No configurations have been created."
    }
}

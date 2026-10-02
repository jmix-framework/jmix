/*
 * Copyright 2026 Haulmont.
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

import io.jmix.search.index.mapping.DisplayedNameDescriptor
import io.jmix.search.index.mapping.ExtendedSearchSettings
import io.jmix.search.index.mapping.IndexMappingConfiguration
import spock.lang.Specification

import java.util.function.Predicate

class IndexConfigurationFormatterTest extends Specification {

    def "format returns null for missing configuration"() {
        expect:
        IndexConfigurationFormatter.format(null) == "null"
    }

    def "format returns index class and entity name"() {
        given:
        def mapping = new IndexMappingConfiguration(
                Mock(io.jmix.core.metamodel.model.MetaClass),
                [:],
                Mock(DisplayedNameDescriptor)
        )
        Predicate<Object> predicate = { true } as Predicate<Object>
        def configuration = new IndexConfiguration(
                "demo_Order",
                String,
                mapping,
                [] as Set,
                predicate,
                ExtendedSearchSettings.empty(),
                true,
                null
        )

        expect:
        IndexConfigurationFormatter.format(configuration) ==
                "indexClass=io.jmix.search.index.IndexConfiguration, entityName='demo_Order', tenantAware=true"
    }
}

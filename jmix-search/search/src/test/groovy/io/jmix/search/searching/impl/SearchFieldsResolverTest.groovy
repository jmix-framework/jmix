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

package io.jmix.search.searching.impl


import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.search.index.IndexConfiguration
import io.jmix.search.index.mapping.IndexConfigurationManager
import io.jmix.search.index.mapping.IndexMappingConfiguration
import io.jmix.search.index.mapping.MappingFieldDescriptor
import io.jmix.security.constraint.PolicyStore
import io.jmix.security.constraint.SecureOperations
import spock.lang.Specification

import static io.jmix.search.searching.impl.SearchFieldsResolver.GETTING_FIELD_WITH_SUBFIELD_WITH_PREFIXES

class SearchFieldsResolverTest extends Specification {
    public static final String FIELD_NAME_1 = "field1"
    public static final String FIELD_NAME_2 = "field2"
    public static final String FIELD_NAME_3 = "field3"

    def "Getting effective fields with security"() {
        given:
        def metaPropertyPath1 = Mock(MetaPropertyPath)
        def mappingFieldDescriptor1 = Mock(MappingFieldDescriptor)
        mappingFieldDescriptor1.getMetaPropertyPath() >> metaPropertyPath1

        and:
        def metaPropertyPath2 = Mock(MetaPropertyPath)
        def mappingFieldDescriptor2 = Mock(MappingFieldDescriptor)
        mappingFieldDescriptor2.getMetaPropertyPath() >> metaPropertyPath2

        and:
        def metaPropertyPath3 = Mock(MetaPropertyPath)
        def mappingFieldDescriptor3 = Mock(MappingFieldDescriptor)
        mappingFieldDescriptor3.getMetaPropertyPath() >> metaPropertyPath3

        and:
        def mapping = Mock(IndexMappingConfiguration)
        mapping.getFields() >> Map.of(
                FIELD_NAME_1, mappingFieldDescriptor1,
                FIELD_NAME_2, mappingFieldDescriptor2,
                FIELD_NAME_3, mappingFieldDescriptor3
        )

        and:
        IndexConfiguration indexConfiguration = Mock()
        indexConfiguration.getMapping() >> mapping

        and:
        def searchFieldsAdapter = Mock(SearchFieldsAdapter)
        searchFieldsAdapter.getFieldsForIndexByPath(metaPropertyPath1, FIELD_NAME_1) >> Set.of(FIELD_NAME_1 + ".subfield1", FIELD_NAME_1 + ".subfield2")
        searchFieldsAdapter.getFieldsForIndexByPath(metaPropertyPath3, FIELD_NAME_3) >> Set.of(FIELD_NAME_3)


        and:
        def secureOperations = Mock(SecureOperations)
        def policyStore = Mock(PolicyStore)
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath1, policyStore) >> true
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath2, policyStore) >> false
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath3, policyStore) >> true

        and:
        SearchFieldsResolver resolver = new SearchFieldsResolver(
                Mock(IndexConfigurationManager),
                secureOperations,
                policyStore,
                searchFieldsAdapter)

        when:
        def fieldsForIndex = resolver.resolveFields(indexConfiguration)

        then:
        fieldsForIndex == Set.of("field1.subfield1", "field1.subfield2", "_instance_name", "field3")
    }

    def "Getting effective fields with security with prefixes"() {
        given:
        def metaPropertyPath1 = Mock(MetaPropertyPath)
        def mappingFieldDescriptor1 = Mock(MappingFieldDescriptor)
        mappingFieldDescriptor1.getMetaPropertyPath() >> metaPropertyPath1

        and:
        def metaPropertyPath2 = Mock(MetaPropertyPath)
        def mappingFieldDescriptor2 = Mock(MappingFieldDescriptor)
        mappingFieldDescriptor2.getMetaPropertyPath() >> metaPropertyPath2

        and:
        def metaPropertyPath3 = Mock(MetaPropertyPath)
        def mappingFieldDescriptor3 = Mock(MappingFieldDescriptor)
        mappingFieldDescriptor3.getMetaPropertyPath() >> metaPropertyPath3

        and:
        def mapping = Mock(IndexMappingConfiguration)
        mapping.getFields() >> Map.of(
                FIELD_NAME_1, mappingFieldDescriptor1,
                FIELD_NAME_2, mappingFieldDescriptor2,
                FIELD_NAME_3, mappingFieldDescriptor3
        )

        and:
        IndexConfiguration indexConfiguration = Mock()
        indexConfiguration.getMapping() >> mapping

        and:
        def searchFieldsAdapter = Mock(SearchFieldsAdapter)
        searchFieldsAdapter.getFieldsForIndexByPath(metaPropertyPath1, FIELD_NAME_1) >> Set.of(FIELD_NAME_1 + ".subfield1", FIELD_NAME_1 + ".subfield2")
        searchFieldsAdapter.getFieldsForIndexByPath(metaPropertyPath3, FIELD_NAME_3) >> Set.of(FIELD_NAME_3)


        and:
        def secureOperations = Mock(SecureOperations)
        def policyStore = Mock(PolicyStore)
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath1, policyStore) >> true
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath2, policyStore) >> false
        secureOperations.isEntityAttrReadPermitted(metaPropertyPath3, policyStore) >> true

        and:
        SearchFieldsResolver resolver = new SearchFieldsResolver(
                Mock(IndexConfigurationManager),
                secureOperations,
                policyStore,
                searchFieldsAdapter)

        when:
        def fieldsForIndex = resolver.resolveFieldsWithSubfields(indexConfiguration, GETTING_FIELD_WITH_SUBFIELD_WITH_PREFIXES)

        then:
        fieldsForIndex == Set.of(
                "field1.subfield1",
                "field1.subfield1.prefix",
                "field1.subfield2",
                "field1.subfield2.prefix",
                "_instance_name",
                "_instance_name.prefix",
                "field3",
                "field3.prefix")
    }

}

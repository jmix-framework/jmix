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

package io.jmix.search.index.mapping.processor.impl.dynattr

import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.dynattr.AttributeDefinition
import io.jmix.dynattr.AttributeType
import io.jmix.dynattr.DynAttrMetadata
import io.jmix.search.utils.PropertyTools
import spock.lang.Specification

import static io.jmix.search.index.annotation.ReferenceFieldsIndexingMode.*
import static java.util.Arrays.asList
import static java.util.Collections.singletonMap

class DynamicAttributesResolverTest extends Specification {

    def "all attributes without references"() {
        given:
        MetaClass metaClass = Mock()

        and:
        def attributeDefinition1 = Mock(AttributeDefinition)
        attributeDefinition1.getCode() >> "attr1"
        attributeDefinition1.getDataType() >> AttributeType.STRING

        and:
        def attributeDefinition2 = Mock(AttributeDefinition)
        attributeDefinition2.getCode() >> "attr2"
        attributeDefinition2.getDataType() >> AttributeType.ENTITY

        and:
        DynAttrMetadata metadata = Mock()
        Collection<AttributeDefinition> attributeDefinitions = asList(attributeDefinition1, attributeDefinition2)
        metadata.getAttributes(metaClass) >> attributeDefinitions

        and:
        PropertyTools propertyTools = Mock()
        propertyTools.findPropertiesByPath(metaClass, "attr1", true) >> singletonMap("key1", Mock(MetaPropertyPath))
        propertyTools.findPropertiesByPath(metaClass, "attr2", true) >> singletonMap("key2", Mock(MetaPropertyPath))

        and:
        def resolver = new DynamicAttributesResolver(metadata, propertyTools)

        when:
        def properties = resolver.resolveEffectivePropertyPaths(metaClass, new String[]{}, new String[]{}, NONE)

        then:
        properties.containsKey("key1")
        !properties.containsKey("key2")
    }

    def "all attributes with references"() {
        given:
        MetaClass metaClass = Mock()

        and:
        def attributeDefinition1 = Mock(AttributeDefinition)
        attributeDefinition1.getCode() >> "attr1"
        attributeDefinition1.getDataType() >> AttributeType.STRING

        and:
        def attributeDefinition2 = Mock(AttributeDefinition)
        attributeDefinition2.getCode() >> "attr2"
        attributeDefinition2.getDataType() >> AttributeType.ENTITY

        and:
        DynAttrMetadata metadata = Mock()
        Collection<AttributeDefinition> attributeDefinitions = asList(attributeDefinition1, attributeDefinition2)
        metadata.getAttributes(metaClass) >> attributeDefinitions

        and:
        PropertyTools propertyTools = Mock()
        propertyTools.findPropertiesByPath(metaClass, "attr1", true) >> singletonMap("key1", Mock(MetaPropertyPath))
        propertyTools.findPropertiesByPath(metaClass, "attr2", true) >> singletonMap("key2", Mock(MetaPropertyPath))

        and:
        def resolver = new DynamicAttributesResolver(metadata, propertyTools)

        when:
        def properties = resolver.resolveEffectivePropertyPaths(metaClass, new String[]{}, new String[]{}, INSTANCE_NAME_ONLY)

        then:
        properties.containsKey("key1")
        properties.containsKey("key2")
    }

    def "attributes. get all"() {
        given:
        MetaClass metaClass = Mock()

        and:
        def attributeDefinition1 = Mock(AttributeDefinition)
        attributeDefinition1.getCode() >> "attr1"
        attributeDefinition1.getDataType() >> AttributeType.STRING

        and:
        def attributeDefinition2 = Mock(AttributeDefinition)
        attributeDefinition2.getCode() >> "attr2"
        attributeDefinition2.getDataType() >> AttributeType.ENTITY

        and:
        DynAttrMetadata metadata = Mock()
        Collection<AttributeDefinition> attributeDefinitions = asList(attributeDefinition1, attributeDefinition2)
        metadata.getAttributes(metaClass) >> attributeDefinitions

        and:
        PropertyTools propertyTools = Mock()
        propertyTools.findPropertiesByPath(metaClass, "attr1", true) >> singletonMap("key1", Mock(MetaPropertyPath))
        propertyTools.findPropertiesByPath(metaClass, "attr2", true) >> singletonMap("key2", Mock(MetaPropertyPath))

        and:
        def resolver = new DynamicAttributesResolver(metadata, propertyTools)

        when:
        def attributes = resolver.getAttributes(metaClass, new String[]{}, new String[]{}, INSTANCE_NAME_ONLY)

        then:
        attributes.size() == 2
    }


}

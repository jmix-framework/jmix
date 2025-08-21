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
import io.jmix.dynattr.CategoryDefinition
import io.jmix.dynattr.DynAttrMetadata
import io.jmix.search.utils.PropertyTools
import spock.lang.Specification

import java.util.stream.Stream

import static io.jmix.search.index.annotation.ReferenceAttributesIndexingMode.*
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
        propertyTools.findPropertiesByPath(metaClass, "+attr1", true) >> singletonMap("key1", Mock(MetaPropertyPath))
        propertyTools.findPropertiesByPath(metaClass, "+attr2", true) >> singletonMap("key2", Mock(MetaPropertyPath))

        and:
        def resolver = new DynamicAttributesResolverImpl(metadata, propertyTools)

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
        propertyTools.findPropertiesByPath(metaClass, "+attr1", true) >> singletonMap("key1", Mock(MetaPropertyPath))
        propertyTools.findPropertiesByPath(metaClass, "+attr2", true) >> singletonMap("key2", Mock(MetaPropertyPath))

        and:
        def resolver = new DynamicAttributesResolverImpl(metadata, propertyTools)

        when:
        def properties = resolver.resolveEffectivePropertyPaths(metaClass, new String[]{}, new String[]{}, INSTANCE_NAME_ONLY)

        then:
        properties.containsKey("key1")
        properties.containsKey("key2")
    }

    def "there are no dynamic attributes"() {
        given:
        MetaClass metaClass = Mock()


        and:
        DynAttrMetadata metadata = Mock()
        metadata.getAttributes(metaClass) >> []

        and:
        PropertyTools propertyTools = Mock()

        and:
        def resolver = new DynamicAttributesResolverImpl(metadata, propertyTools)

        when:
        def properties = resolver.resolveEffectivePropertyPaths(metaClass, new String[]{}, new String[]{}, INSTANCE_NAME_ONLY)

        then:
        properties.size() == 0
    }

    def "Attributes of not supported types excluding."() {
        given:
        MetaClass metaClass = Mock()
        and:
        def category1 = createCategoryWithAttributes(
                "category1",
                Map.of(
                        "textAttr", AttributeType.STRING,
                        "intAttr", AttributeType.INTEGER,
                        "doubleAttr", AttributeType.DOUBLE,
                        "decimalAttr", AttributeType.DECIMAL,
                        "dateAttr", AttributeType.DATE,
                        "dateWithoutTimeAttr", AttributeType.DATE_WITHOUT_TIME,
                        "booleanAttr", AttributeType.BOOLEAN,
                        "entityAttr", AttributeType.ENTITY,
                        "enumerationAttr", AttributeType.ENUMERATION
                )
        )

        def category2 = createCategoryWithAttributes(
                "category2",
                Map.of(
                        "textAttr", AttributeType.STRING,
                        "intAttr", AttributeType.INTEGER,
                        "doubleAttr", AttributeType.DOUBLE,
                        "decimalAttr", AttributeType.DECIMAL,
                        "dateAttr", AttributeType.DATE,
                        "dateWithoutTimeAttr", AttributeType.DATE_WITHOUT_TIME,
                        "booleanAttr", AttributeType.BOOLEAN,
                        "entityAttr", AttributeType.ENTITY,
                        "enumerationAttr", AttributeType.ENUMERATION
                )
        )

        and:
        DynAttrMetadata metadata = Mock()
        metadata.getAttributes(metaClass) >> Stream.of(category1, category2)
                .map { category -> category.getAttributeDefinitions() }
                .flatMap { col -> col.stream() }
                .toList()

        metadata.getCategories(metaClass) >> asList(category1, category2)

        and:
        def resolver = new DynamicAttributesResolverImpl(metadata, Mock(PropertyTools))

        when:
        def attributes = resolver.getAttributes(metaClass, excludeCategories.toArray() as String[], excludeParameters.toArray() as String[], referenceMode)
        def attributeNames = attributes
                .stream()
                .map { attributeDefinition -> attributeDefinition.getCode() }
                .toList()

        then:
        attributeNames == resultAttributes

        where:
        excludeCategories          | excludeParameters                                 | referenceMode      || resultAttributes
        []                         | []                                                | NONE               || ["category2textAttr", "category2enumerationAttr", "category1enumerationAttr", "category1textAttr"]
        []                         | []                                                | INSTANCE_NAME_ONLY || ["category2textAttr", "category2enumerationAttr", "category2entityAttr", "category1enumerationAttr", "category1textAttr", "category1entityAttr"]
        ["category1"]              | []                                                | NONE               || ["category2textAttr", "category2enumerationAttr"]
        ["category1"]              | []                                                | INSTANCE_NAME_ONLY || ["category2textAttr", "category2enumerationAttr", "category2entityAttr"]
        ["category1"]              | ["category2textAttr", "category2enumerationAttr"] | NONE               || []
        []                         | ["category2textAttr", "category1textAttr"]        | NONE               || ["category2enumerationAttr", "category1enumerationAttr"]
        ["category1", "category2"] | ["category2textAttr", "category2enumerationAttr"] | NONE               || []
        ["category1", "category2"] | ["category1textAttr", "category2enumerationAttr"] | NONE               || []
        ["category2"]              | ["category2textAttr", "category2enumerationAttr"] | NONE               || ["category1enumerationAttr", "category1textAttr"]


    }

    CategoryDefinition createCategoryWithAttributes(String categoryName, Map<String, AttributeType> attributes) {
        Collection<AttributeDefinition> attributeDefinitions = new ArrayList<>();
        for (Map.Entry<String, AttributeType> attrData : attributes.entrySet()) {
            attributeDefinitions.add(createAttribute(categoryName + attrData.getKey(), attrData.getValue()))
        }

        CategoryDefinition categoryDefinition = Mock()
        categoryDefinition.getName() >> categoryName
        categoryDefinition.getAttributeDefinitions() >> attributeDefinitions
        return categoryDefinition
    }

    AttributeDefinition createAttribute(String attributeCode, AttributeType attributeType) {
        def attributeDefinition = Mock(AttributeDefinition)
        attributeDefinition.getCode() >> attributeCode
        attributeDefinition.getDataType() >> attributeType
        return attributeDefinition
    }
}

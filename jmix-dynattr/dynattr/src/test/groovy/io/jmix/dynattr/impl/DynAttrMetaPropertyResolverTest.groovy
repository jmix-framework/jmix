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

package io.jmix.dynattr.impl

import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.dynattr.AttributeDefinition
import io.jmix.dynattr.DynAttrMetadata
import io.jmix.core.metamodel.model.MetaClass
import spock.lang.Specification

class DynAttrMetaPropertyResolverTest extends Specification {

    def "ResolveMetaPropertyOrNull. There is regular MetaProperty."() {
        given:
        def propertyName = "somePropertyName"
        def metaProperty = Mock(MetaProperty)
        def metaClass = Mock(MetaClass)

        and:
        def dynAttrMetadata = Mock(DynAttrMetadata)
        metaClass.findProperty(propertyName) >> metaProperty

        when:
        def resolver = new DynAttrMetaPropertyResolver(dynAttrMetadata)
        def resultMetaProperty = resolver.resolveMetaPropertyOrNull(metaClass, propertyName)

        then:
        resultMetaProperty == metaProperty
    }

    def "ResolveMetaPropertyOrNull. There is no regular MetaProperty but it is not dynamic."() {
        given:
        def propertyName = "somePropertyName"
        def metaClass = Mock(MetaClass)

        and:
        def dynAttrMetadata = Mock(DynAttrMetadata)
        metaClass.findProperty(propertyName) >> null

        when:
        def resolver = new DynAttrMetaPropertyResolver(dynAttrMetadata)
        def resultMetaProperty = resolver.resolveMetaPropertyOrNull(metaClass, propertyName)

        then:
        resultMetaProperty == null
    }

    def "ResolveMetaPropertyOrNull. Looks like dynamic but not found"() {
        given:
        def propertyName = "+somePropertyName"

        and:
        def metaClass = Mock(MetaClass)
        metaClass.findProperty(propertyName) >> null

        and:
        def dynAttrMetadata = Mock(DynAttrMetadata)
        dynAttrMetadata.getAttributeByCode(metaClass, "somePropertyName") >> Optional.empty()

        when:
        def resolver = new DynAttrMetaPropertyResolver(dynAttrMetadata)
        def resultMetaProperty = resolver.resolveMetaPropertyOrNull(metaClass, propertyName)

        then:
        resultMetaProperty == null
    }

    def "ResolveMetaPropertyOrNull. Normal flow"() {
        given:
        def propertyName = "+somePropertyName"

        and:
        def metaClass = Mock(MetaClass)
        metaClass.findProperty(propertyName) >> null

        and:
        def attributeDefinition = Mock(AttributeDefinition)
        def metaProperty = Mock(MetaProperty)
        attributeDefinition.getMetaProperty() >> metaProperty

        and:
        def dynAttrMetadata = Mock(DynAttrMetadata)
        dynAttrMetadata.getAttributeByCode(metaClass, "somePropertyName") >> Optional.of(attributeDefinition)

        when:
        def resolver = new DynAttrMetaPropertyResolver(dynAttrMetadata)
        def resultMetaProperty = resolver.resolveMetaPropertyOrNull(metaClass, propertyName)

        then:
        resultMetaProperty == metaProperty
    }
}

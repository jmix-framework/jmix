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

package io.jmix.search.listener

import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.search.listener.dynattr.DynamicAttributeReferenceFieldResolver
import spock.lang.Specification

class DynamicAttributeReferenceFieldResolverTest extends Specification {

    def "GetFieldName. Composite key"() {
        given:
        def metaClass = Mock(MetaClass)
        metaClass.getName() >> "some_EntityName"

        and:
        def metadataTools = Mock(MetadataTools)
        metadataTools.hasCompositePrimaryKey(metaClass) >> true

        when:
        def resolver = new DynamicAttributeReferenceFieldResolver(metadataTools)
        resolver.getFieldName(metaClass)

        then:
        def e = thrown(IllegalStateException)
        e.message == "Composite keys are not supported in the dynamic attributes. The entity type is some_EntityName."
    }

    def "GetFieldName. Supported type"() {
        given:
        def metaClass = Mock(MetaClass)

        and:
        def metaProperty = Mock(MetaProperty)
        metaProperty.getJavaType() >> keyClass

        and:
        def metadataTools = Mock(MetadataTools)
        metadataTools.hasCompositePrimaryKey(metaClass) >> false
        metadataTools.getPrimaryKeyProperty(metaClass) >> metaProperty

        when:
        def resolver = new DynamicAttributeReferenceFieldResolver(metadataTools)
        def resolvedFieldName = resolver.getFieldName(metaClass)

        then:
        resolvedFieldName == fieldName

        where:
        keyClass   || fieldName
        UUID.class || "entityId"
        String.class || "stringEntityId"
        Integer.class || "intEntityId"
        Long.class || "longEntityId"
    }

}

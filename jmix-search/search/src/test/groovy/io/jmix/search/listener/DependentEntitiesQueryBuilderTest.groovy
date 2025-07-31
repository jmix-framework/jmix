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

package io.jmix.search.listener

import io.jmix.core.Id
import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.annotation.JmixEntity
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.core.metamodel.model.MetaPropertyPath
import io.jmix.search.utils.PropertyTools
import jakarta.persistence.Entity
import jakarta.persistence.OneToMany
import spock.lang.Specification

import java.lang.reflect.AnnotatedElement

class DependentEntitiesQueryBuilderTest extends Specification {

    def "build query for static attributes. one level"() {
        given:
        def targetEntityId = UUID.randomUUID()

        and:
        def annotatedElement = Mock(AnnotatedElement)
        annotatedElement.isAnnotationPresent(OneToMany) >> true
        def metaProperty = Mock(MetaProperty)
        metaProperty.getAnnotatedElement() >> annotatedElement
        metaProperty.getName() >> "propertyName"

        and:
        def metaPropertyPath = Mock(MetaPropertyPath)
        metaPropertyPath.getMetaProperties() >> [metaProperty].toArray()
        metaPropertyPath.getFirstPropertyName() >> "propertyName"

        and:
        def referencedMetaClass = Mock(MetaClass)
        referencedMetaClass.getName() >> "some_entityName"

        and:
        def targetMetaClass = Mock(MetaClass)
        targetMetaClass.getJavaClass() >> ReferenceEntity

        and:
        def metadataTools = Mock(MetadataTools)
        metadataTools.getPrimaryKeyName(targetMetaClass) >> "pk_name"


        when:
        def query = new DependentEntitiesQueryBuilder(metadataTools, Mock(DynamicAttributeReferenceFieldResolver))
                .loadEntity(referencedMetaClass)
                .byProperty(metaPropertyPath)
                .dependedOn(targetMetaClass, Id.of(targetEntityId, ReferenceEntity))
                .buildQuery()

        def firstParameter = query.getParameters().iterator().next()

        then:
        query.getQuery() == "select e1 from some_entityName e1 join e1.propertyName e2 where e2.pk_name = :ref"
        query.getParameters().size() == 1
        firstParameter.getKey() == "ref"
        firstParameter.getValue() == targetEntityId
    }


    def "build query for static attributes. two levels"() {
        given:
        def targetEntityId = UUID.randomUUID()

        and:
        def annotatedElement1 = Mock(AnnotatedElement)
        annotatedElement1.isAnnotationPresent(OneToMany) >> true
        def metaProperty1 = Mock(MetaProperty)
        metaProperty1.getAnnotatedElement() >> annotatedElement1
        metaProperty1.getName() >> "property1Name"

        and:
        def annotatedElement2 = Mock(AnnotatedElement)
        annotatedElement2.isAnnotationPresent(OneToMany) >> true
        def metaProperty2 = Mock(MetaProperty)
        metaProperty2.getAnnotatedElement() >> annotatedElement2
        metaProperty2.getName() >> "property2Name"

        and:
        def metaPropertyPath = Mock(MetaPropertyPath)
        metaPropertyPath.getMetaProperties() >> [metaProperty1, metaProperty2].toArray()
        metaPropertyPath.getFirstPropertyName() >> "property1Name"

        and:
        def targetMetaClass = Mock(MetaClass)
        targetMetaClass.getJavaClass() >> ReferenceEntity

        and:
        def metadataTools = Mock(MetadataTools)
        metadataTools.getPrimaryKeyName(targetMetaClass) >> "pk_name"

        and:
        def referencedMetaClass = Mock(MetaClass)
        referencedMetaClass.getName() >> "some_entityName"

        when:
        def query = new DependentEntitiesQueryBuilder(metadataTools, Mock(DynamicAttributeReferenceFieldResolver))
                .loadEntity(referencedMetaClass)
                .byProperty(metaPropertyPath)
                .dependedOn(targetMetaClass, Id.of(targetEntityId, ReferenceEntity))
                .buildQuery()

        def firstParameter = query.getParameters().iterator().next()

        then:
        query.getQuery() == "select e1 from some_entityName e1 join e1.property1Name e2 join e2.property2Name e3 where e3.pk_name = :ref"
        query.getParameters().size() == 1
        firstParameter.getKey() == "ref"
        firstParameter.getValue() == targetEntityId
    }

    def "build query for dynamic attributes"() {
        given:
        def targetEntityId = UUID.randomUUID()

        and:
        def annotatedElement1 = Mock(AnnotatedElement)
        annotatedElement1.isAnnotationPresent(_) >> false
        def metaProperty1 = Mock(MetaProperty)
        metaProperty1.getAnnotatedElement() >> annotatedElement1
        metaProperty1.getName() >> "+dynamicPropertyName"

        and:
        def metaPropertyPath = Mock(MetaPropertyPath)
        metaPropertyPath.getMetaProperties() >> [metaProperty1].toArray()
        metaPropertyPath.getFirstPropertyName() >> "+dynamicPropertyName"

        and:
        def referencedMetaClass = Mock(MetaClass)
        referencedMetaClass.getName() >> "some_entityName"

        and:
        def targetMetaClass = Mock(MetaClass)
        targetMetaClass.getJavaClass() >> ReferenceEntity

        and:
        def metadataTools = Mock(MetadataTools)
        metadataTools.getPrimaryKeyName(targetMetaClass) >> "pk_name"

        and:
        def resolver = Mock(DynamicAttributeReferenceFieldResolver)
        resolver.getFieldName(_) >> "entityId"

        when:
        def query = new DependentEntitiesQueryBuilder(metadataTools, resolver)
                .loadEntity(referencedMetaClass)
                .byProperty(metaPropertyPath)
                .dependedOn(targetMetaClass, Id.of(targetEntityId, ReferenceEntity))
                .buildQuery()

        def firstParameter = query.getParameters().iterator().next()

        then:
        query.getQuery() == "select e1 from some_entityName e1 where exists " +
                "(select r from dynat_CategoryAttributeValue r " +
                    "where r.entityValue.entityId =:ref and r.entity.entityId = e1.pk_name)"
        query.getParameters().size() == 1
        firstParameter.getKey() == "ref"
        firstParameter.getValue() == targetEntityId
    }

    @Entity
    @JmixEntity
    private static class ReferenceEntity {

    }
}

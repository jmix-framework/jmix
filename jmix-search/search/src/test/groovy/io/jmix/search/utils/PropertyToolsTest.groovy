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

package io.jmix.search.utils

import io.jmix.core.MetadataTools
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.core.metamodel.model.MetaPropertyPath
import jakarta.persistence.Transient
import spock.lang.Specification

import java.lang.reflect.AnnotatedElement

class PropertyToolsTest extends Specification {

    public static final String PATH_STRING_SIMPLE_FIELD = "fieldName"
    public static final String PATH_STRING_WIELD_FROM_REFERENCE = "ref1.fieldName"

    //add logging check
    def "FindPropertiesByPath with dynamics and with wildcard"() {

        given:
        def metaClassMock = Mock(MetaClass)
        def metadataToolsMock = Mock(MetadataTools)
        metadataToolsMock.resolveMetaPropertyPathOrNull(metaClassMock, pathString) >> metadataToolsResult

        and:
        def tools = new PropertyTools(metadataToolsMock)


        when:
        def path = tools.findPropertiesByPaths(metaClassMock, pathString, true)

        then:
        path == result

        where:
        pathString                       | metadataToolsResult               || result
        PATH_STRING_SIMPLE_FIELD         | createPathForDynamicAttributes(1) || Map.of(pathString, metadataToolsResult)
        PATH_STRING_WIELD_FROM_REFERENCE | createPathForDynamicAttributes(2) || Map.of(pathString, metadataToolsResult)
        PATH_STRING_WIELD_FROM_REFERENCE | null                              || Collections.emptyMap()

    }

    private MetaPropertyPath createPathForDynamicAttributes(int propertiesCount) {
        def metaPropertyPathMock = Mock(MetaPropertyPath)
        def properties = new ArrayList<MetaProperty>()
        for (i in 1..propertiesCount) {
            properties.add(createMetaProperty())
        }
        metaPropertyPathMock.getMetaProperties() >> properties.toArray()
        metaPropertyPathMock
    }

    private io.jmix.core.metamodel.model.MetaProperty createMetaProperty() {
        def metaProperty = Mock(io.jmix.core.metamodel.model.MetaProperty)
        metaProperty.getAnnotatedElement() >> createAnnotatedElement()
        metaProperty
    }

    private AnnotatedElement createAnnotatedElement() {
        def annotatedElement = Mock(AnnotatedElement)
        annotatedElement.isAnnotationPresent(Transient) >> false
        return annotatedElement
    }
}

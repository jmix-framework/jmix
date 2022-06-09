/*
 * Copyright 2020 Haulmont.
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

package metadata

import io.jmix.core.CoreConfiguration
import io.jmix.core.Metadata
import io.jmix.core.MetadataTools
import io.jmix.core.entity.EntityPropertyChangeListener
import io.jmix.core.entity.EntityValues
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.core.metamodel.model.Range
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.app.entity.model_objects.*

import java.time.LocalDate

@ContextConfiguration(classes = [CoreConfiguration, TestAddon1Configuration, TestAppConfiguration])
class JmixEntitiesMetadataTest extends Specification {

    @Autowired
    Metadata metadata
    @Autowired
    MetadataTools metadataTools
    @Autowired
    DatatypeRegistry datatypeRegistry

    def "model object without explicit name"() {
        when:
        def metaClass = metadata.getClass(OrderLineObject)

        then:
        metaClass.getName() == 'OrderLineObject'
    }

    def "mandatory property"() {
        when:
        def metaClass = metadata.getClass(CustomerObject)

        then:
        def metaProperty = metaClass.getProperty('name')
        metaProperty.mandatory
    }

    def "non-annotated properties"() {
        def metaClass = metadata.getClass(OrderObject)

        when:
        def dateProperty = metaClass.findProperty('date')

        then:
        dateProperty != null
        !dateProperty.mandatory
        dateProperty.range.isDatatype()
        dateProperty.range.asDatatype() == datatypeRegistry.find(LocalDate)
        dateProperty.range.cardinality == Range.Cardinality.NONE

        when:
        def customerProperty = metaClass.findProperty('customer')

        then:
        customerProperty != null
        !customerProperty.mandatory
        customerProperty.range.isClass()
        customerProperty.range.asClass().javaClass == CustomerObject
        customerProperty.range.cardinality == Range.Cardinality.MANY_TO_ONE

        when:
        def linesProperty = metaClass.findProperty('lines')

        then:
        linesProperty != null
        !linesProperty.mandatory
        linesProperty.range.isClass()
        linesProperty.range.asClass().javaClass == OrderLineObject
        linesProperty.range.cardinality == Range.Cardinality.ONE_TO_MANY
    }

    def "annotated properties are enhanced"() {
        def customer = metadata.create(CustomerObject)

        EntityPropertyChangeListener listener = Mock()
        customer.__getEntityEntry().addPropertyChangeListener(listener)

        when:
        customer.setName('abc')

        then:
        1 * listener.propertyChanged(_)
    }

    def "non-annotated properties are enhanced"() {
        def order = metadata.create(OrderObject)

        EntityPropertyChangeListener listener = Mock()
        order.__getEntityEntry().addPropertyChangeListener(listener)

        when:
        order.setNumber('1')

        then:
        1 * listener.propertyChanged(_)
    }

    def "non-annotated properties are not included if annotatedPropertiesOnly = true"() {
        when:
        def metaClass = metadata.getClass(CustomerObject)

        then:
        def metaProperty = metaClass.findProperty('anObject')
        metaProperty == null

    }

    def "nullable id"() {
        when:
        def metaClass = metadata.getClass(CustomerObjectWithNullableId)
        def idProperty = metadataTools.getPrimaryKeyProperty(metaClass)

        then:
        idProperty != null
        idProperty.name == 'id'
        idProperty.range.asDatatype().javaClass == String

        when:
        def customer = metadata.create(CustomerObjectWithNullableId)
        customer.setId('abc')

        then:
        EntityValues.getId(customer) == 'abc'

        when:
        EntityValues.setId(customer, 'def')

        then:
        customer.getId() == 'def'
    }

    def "generated id"() {
        when:
        def metaClass = metadata.getClass(CustomerObjectWithGeneratedId)
        def idProperty = metadataTools.getPrimaryKeyProperty(metaClass)

        then:
        idProperty != null
        idProperty.name == 'id'
        idProperty.range.asDatatype().javaClass == UUID

        when:
        def customer = metadata.create(CustomerObjectWithGeneratedId)

        then:
        customer.getId() != null
        EntityValues.getId(customer) == customer.getId()
    }

    def "DTO Enum property loaded to metadata"() {
        expect: "Property 'orderState' loaded to metadata and has correct type"
        metadata.getClass(OrderObject).getProperty("orderState").type == MetaProperty.Type.ENUM
    }
}

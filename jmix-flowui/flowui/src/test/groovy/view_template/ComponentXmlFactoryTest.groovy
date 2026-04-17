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

package view_template

import io.jmix.core.Metadata
import io.jmix.core.metamodel.model.MetaClass
import io.jmix.core.metamodel.model.MetaProperty
import io.jmix.flowui.action.entitypicker.EntityClearAction
import io.jmix.flowui.action.entitypicker.EntityLookupAction
import io.jmix.flowui.action.entitypicker.EntityOpenCompositionAction
import io.jmix.flowui.view.template.impl.ComponentXmlFactory
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.viewtemplate.ComponentXmlDatatypesEntity
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ComponentXmlFactoryTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata

    @Autowired
    ComponentXmlFactory componentXmlFactory

    MetaClass datatypesMetaClass

    @Override
    void setup() {
        datatypesMetaClass = metadata.getClass(ComponentXmlDatatypesEntity.class)
    }

    def "Create component XML for datatype property"() {
        when:
        Element element = createElement(propertyName)

        then:
        element.name == elementName
        assertDataBinding(element, propertyName, null)

        where:
        propertyName           | elementName
        'stringValue'          | 'textField'
        'uuidValue'            | 'textField'
        'booleanValue'         | 'checkbox'
        'sqlDateValue'         | 'datePicker'
        'localDateValue'       | 'datePicker'
        'sqlTimeValue'         | 'timePicker'
        'localTimeValue'       | 'timePicker'
        'offsetTimeValue'      | 'timePicker'
        'dateValue'            | 'dateTimePicker'
        'localDateTimeValue'   | 'dateTimePicker'
        'offsetDateTimeValue'  | 'dateTimePicker'
        'shortValue'           | 'textField'
        'integerValue'         | 'textField'
        'longValue'            | 'textField'
        'bigIntegerValue'      | 'textField'
        'bigDecimalValue'      | 'textField'
        'floatValue'           | 'textField'
        'doubleValue'          | 'textField'
        'characterValue'       | 'textField'
        'uriValue'             | 'textField'
    }

    def "Create component XML for Lob string property"() {
        when:
        Element element = createElement('lobValue')

        then:
        element.name == 'textArea'
        assertDataBinding(element, 'lobValue', null)
    }

    def "Create component XML for file datatype properties"() {
        when:
        Element element = createElement(propertyName)

        then:
        element.name == elementName
        assertDataBinding(element, propertyName, null)
        element.attributeValue('fileNameVisible') == 'true'
        element.attributeValue('clearButtonVisible') == 'true'

        where:
        propertyName      | elementName
        'fileRefValue'    | 'fileStorageUploadField'
        'byteArrayValue'  | 'fileUploadField'
    }

    def "Create component XML for enum property"() {
        when:
        Element element = createElement('status')

        then:
        element.name == 'select'
        assertDataBinding(element, 'status', null)
    }

    def "Create component XML for many-to-one reference property"() {
        given:
        MetaProperty property = metadata.getClass(Order.class).getProperty('customer')

        when:
        Element element = createElement(property, null)

        then:
        element.name == 'entityPicker'
        assertDataBinding(element, 'customer', null)

        and:
        Element actionsElement = element.element('actions')
        List<Element> actions = actionsElement.elements('action')
        actions*.attributeValue('id') == ['entityLookup', 'entityClear']
        actions*.attributeValue('type') == [EntityLookupAction.ID, EntityClearAction.ID]
    }

    def "Create component XML for single composition property"() {
        given:
        MetaProperty property = datatypesMetaClass.getProperty('compositionValue')

        when:
        Element element = createElement(property, null)

        then:
        element.name == 'entityPicker'
        assertDataBinding(element, 'compositionValue', null)

        and:
        Element actionsElement = element.element('actions')
        List<Element> actions = actionsElement.elements('action')
        actions*.attributeValue('id') == ['entityOpenComposition', 'entityClear']
        actions*.attributeValue('type') == [EntityOpenCompositionAction.ID, EntityClearAction.ID]
    }

    def "Create component XML with data container"() {
        when:
        Element element = createElement('stringValue', 'testDc')

        then:
        element.name == 'textField'
        assertDataBinding(element, 'stringValue', 'testDc')
    }

    def "Ignore embedded property"() {
        given:
        MetaProperty property = metadata.getClass(Customer.class).getProperty('address')

        expect:
        componentXmlFactory.createComponentXml(property, null) == ''
    }

    def "Fail on collection property"() {
        given:
        MetaProperty property = metadata.getClass(Order.class).getProperty('orderLines')

        when:
        componentXmlFactory.createComponentXml(property, null)

        then:
        thrown(IllegalArgumentException)
    }

    Element createElement(String propertyName, String dataContainerId = null) {
        return createElement(datatypesMetaClass.getProperty(propertyName), dataContainerId)
    }

    Element createElement(MetaProperty property, String dataContainerId) {
        String xml = componentXmlFactory.createComponentXml(property, dataContainerId)
        return DocumentHelper.parseText(xml).rootElement
    }

    static void assertDataBinding(Element element, String propertyName, String dataContainerId) {
        assert element.attributeValue('id') == propertyName + 'Field'
        assert element.attributeValue('property') == propertyName
        assert element.attributeValue('dataContainer') == dataContainerId
    }
}

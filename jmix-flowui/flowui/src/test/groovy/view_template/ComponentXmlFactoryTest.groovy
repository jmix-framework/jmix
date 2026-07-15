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
import test_support.entity.lookup_field.LfNoteHolder
import test_support.entity.lookup_field.LfOrder
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
        thrown(UnsupportedOperationException)
    }

    def "VIEW annotation produces entityPicker with default association actions"() {
        when:
        def element = lfElement('viewCountry')

        then:
        element.name == 'entityPicker'
        element.element('actions').elements('action')*.attributeValue('id') == ['entityLookup', 'entityClear']
    }

    def "class VIEW annotation actions are used on entityPicker"() {
        when:
        def element = lfElement('city')

        then:
        element.name == 'entityPicker'
        element.element('actions').elements('action')*.attributeValue('type') ==
                ['entity_lookup', 'entity_open', 'entity_clear']
    }

    def "field DROPDOWN overrides class VIEW and produces entityComboBox with byInstanceName itemsQuery and inherited class actions"() {
        when:
        def element = lfElement('dropdownCity')

        then:
        element.name == 'entityComboBox'
        def iq = element.element('itemsQuery')
        iq.attributeValue('class') == 'test_support.entity.lookup_field.LfCity'
        iq.attributeValue('byInstanceName') == 'true'

        and:
        def actions = element.element('actions').elements('action')
        actions*.attributeValue('id') == ['entity_lookup', 'entity_open', 'entity_clear']
        actions*.attributeValue('type') == ['entity_lookup', 'entity_open', 'entity_clear']
    }

    def "eager class DROPDOWN maps to byInstanceName itemsQuery"() {
        when:
        def element = lfElement('country')

        then:
        element.name == 'entityComboBox'
        element.element('itemsQuery').attributeValue('byInstanceName') == 'true'
    }

    def "byInstanceName class DROPDOWN produces byInstanceName itemsQuery ignoring searchStringFormat"() {
        when:
        def element = lfElement('product')

        then:
        element.name == 'entityComboBox'
        def iq = element.element('itemsQuery')
        iq.attributeValue('byInstanceName') == 'true'
        iq.attributeValue('searchStringFormat') == null
    }

    def "explicit query class DROPDOWN produces query itemsQuery"() {
        when:
        def element = lfElement('supplier')

        then:
        element.name == 'entityComboBox'
        def iq = element.element('itemsQuery')
        iq.attributeValue('byInstanceName') == null
        iq.element('query').text.contains(':searchString')
        iq.attributeValue('searchStringFormat') == '(?i)%${inputString}%'
        iq.attributeValue('escapeValueForLike') == 'true'
    }

    def "explicit query without searchString degrades to byInstanceName itemsQuery"() {
        when:
        def element = lfElement('broken')

        then:
        element.name == 'entityComboBox'
        element.element('itemsQuery').attributeValue('byInstanceName') == 'true'
    }

    def "byInstanceName with no string instance-name properties degrades to entityPicker"() {
        when:
        def element = lfElement('event')

        then:
        element.name == 'entityPicker'
    }

    def "itemsQuery on VIEW type produces entityPicker"() {
        when:
        def element = lfElement('tag')

        then:
        element.name == 'entityPicker'
    }

    def "DROPDOWN on noop-store entity degrades to entityPicker"() {
        given:
        def property = metadata.getClass(LfNoteHolder).getProperty('note')

        when:
        def element = createElement(property, null)

        then:
        element.name == 'entityPicker'
    }

    Element lfElement(String propertyName) {
        def property = metadata.getClass(LfOrder).getProperty(propertyName)
        return createElement(property, null)
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

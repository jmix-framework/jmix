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

package localized_string

import io.jmix.core.Metadata
import io.jmix.core.metamodel.datatype.DatatypeRegistry
import io.jmix.flowui.action.multivaluepicker.MultiValueSelectAction
import io.jmix.flowui.action.valuepicker.LocalizedStringEditAction
import io.jmix.flowui.action.valuepicker.ValueClearAction
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.ComponentGenerationContext
import io.jmix.flowui.component.SupportsDatatype
import io.jmix.flowui.component.UiComponentsGenerator
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilterSupport
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.component.valuepicker.JmixMultiValuePicker
import io.jmix.flowui.component.valuepicker.JmixValuePicker
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.view.template.impl.ComponentXmlFactory
import org.dom4j.DocumentHelper
import org.dom4j.Element
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.localized_string.LsItem
import test_support.localized_string.StrictCodeDatatype
import test_support.spec.FlowuiTestSpecification

// Two locales: with a single one the strategy declines, which LocalizedStringSingleLocaleTest covers.
@SpringBootTest(properties = ["jmix.core.available-locales=en,de"])
class LocalizedStringGenerationStrategyTest extends FlowuiTestSpecification {

    @Autowired
    UiComponentsGenerator uiComponentsGenerator
    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata
    @Autowired
    DataComponents dataComponents
    @Autowired
    PropertyFilterSupport propertyFilterSupport
    @Autowired
    DatatypeRegistry datatypeRegistry
    @Autowired
    ComponentXmlFactory componentXmlFactory

    def generate(String property) {
        def metaClass = metadata.getClass(LsItem)
        def context = new ComponentGenerationContext(metaClass, property)
        def container = dataComponents.createInstanceContainer(LsItem)
        context.setValueSource(new ContainerValueSource(container, property))
        return uiComponentsGenerator.generate(context)
    }

    Element templateElement(String property) {
        def xml = componentXmlFactory.createComponentXml(metadata.getClass(LsItem).getProperty(property), null)
        return DocumentHelper.parseText(xml).rootElement
    }

    PropertyFilter propertyFilter(String property, PropertyFilter.Operation operation) {
        def container = dataComponents.createCollectionContainer(LsItem)
        def loader = dataComponents.createCollectionLoader()
        loader.container = container
        def filter = uiComponents.create(PropertyFilter)
        filter.autoApply = false
        filter.dataLoader = loader
        filter.property = property
        filter.operation = operation
        return filter
    }

    def "a localized string property gets a full-width value picker with the edit and clear actions"() {
        when:
        def component = generate('name')

        then:
        component instanceof JmixValuePicker
        (component as JmixValuePicker).getAction(LocalizedStringEditAction.ID) instanceof LocalizedStringEditAction
        (component as JmixValuePicker).getAction(ValueClearAction.ID) instanceof ValueClearAction
        (component as JmixValuePicker).width == '100%'
    }

    def "a plain string property still gets a text field"() {
        expect:
        generate('code') instanceof TypedTextField
    }

    def "PropertyFilter offers the string operations for a localized string property"() {
        expect:
        propertyFilterSupport.getAvailableOperations(metadata.getClass(LsItem), 'name').containsAll(
                [PropertyFilter.Operation.CONTAINS,
                 PropertyFilter.Operation.EQUAL,
                 PropertyFilter.Operation.STARTS_WITH])
    }

    def "switching the operation of a PropertyFilter keeps the entered text, as it does for a plain string"() {
        given:
        def filter = propertyFilter(property, PropertyFilter.Operation.CONTAINS)
        filter.value = 'text'

        when: "both operations are served by the same text field, so it must not be recreated"
        filter.operation = PropertyFilter.Operation.EQUAL

        then:
        filter.value == 'text'

        where:
        property << ['name', 'code']
    }

    def "the value of an EQUAL PropertyFilter is entered as a plain string, as it is for a plain string"() {
        when:
        def filter = propertyFilter(property, PropertyFilter.Operation.EQUAL)

        then: "a filter value is a search text, not a localized value, so the field neither resolves nor parses it"
        (filter.valueComponent as SupportsDatatype).datatype == datatypeRegistry.get(String)

        where:
        property << ['name', 'code']
    }

    def "the values of an IN_LIST PropertyFilter are entered as plain strings, as they are for a plain string"() {
        when:
        def filter = propertyFilter(property, PropertyFilter.Operation.IN_LIST)
        def picker = filter.valueComponent as JmixMultiValuePicker

        then:
        (picker.getAction(MultiValueSelectAction.ID) as MultiValueSelectAction).datatype ==
                datatypeRegistry.get(String)

        where:
        property << ['name', 'code']
    }

    def "switching the operation of a PropertyFilter recreates the field of another datatype derived from the string one"() {
        given: "a datatype that parses strictly, so the partial text of a contains must not reach its parser"
        def filter = propertyFilter('strictCode', PropertyFilter.Operation.CONTAINS)
        filter.value = '12'

        when:
        filter.operation = PropertyFilter.Operation.EQUAL

        then: "the value component is recreated with the property's datatype and the text is dropped"
        filter.value == null
        (filter.valueComponent as SupportsDatatype).datatype instanceof StrictCodeDatatype
    }

    def "a view template gives a localized property the value picker with the edit and clear actions"() {
        when:
        def element = templateElement(property)

        then: "a plain field would save the edited text as the whole value, dropping the other locales"
        element.name == 'valuePicker'
        element.attributeValue('property') == property
        element.element('actions').elements('action')*.attributeValue('type') ==
                [LocalizedStringEditAction.ID, ValueClearAction.ID]

        where:
        property << ['name', 'description']
    }

    def "a view template still gives a plain string property a text field"() {
        expect:
        templateElement('code').name == 'textField'
    }
}

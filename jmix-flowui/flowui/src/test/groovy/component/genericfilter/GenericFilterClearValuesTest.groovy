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

package component.genericfilter

import com.vaadin.flow.component.Component
import com.vaadin.flow.dom.DomEvent
import com.vaadin.flow.internal.JacksonUtils
import com.vaadin.flow.internal.nodefeature.ElementListenerMap
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap
import component.genericfilter.view.GfClearValuesTestView
import io.jmix.flowui.action.genericfilter.GenericFilterClearValuesAction
import io.jmix.flowui.component.genericfilter.GenericFilter
import io.jmix.flowui.component.propertyfilter.PropertyFilter
import io.jmix.flowui.component.datepicker.TypedDatePicker
import io.jmix.flowui.component.textfield.TypedTextField
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

/**
 * The 'Clear values' action must clear a condition whose input cannot be converted to the
 * property type, no matter whether the condition already holds a value.
 */
@SpringBootTest
class GenericFilterClearValuesTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.genericfilter.view")
    }

    def "clear values clears an unconvertable input typed into an empty numeric condition"() {
        given: "a numeric condition with an empty value"
        def view = navigateToView(GfClearValuesTestView)
        GenericFilter filter = view.genericFilter
        PropertyFilter<BigDecimal> condition = view.amountCondition
        TypedTextField<BigDecimal> valueComponent = condition.valueComponent as TypedTextField<BigDecimal>

        when: "the user types text that is not a number"
        typeFromClient(valueComponent, "abc")

        then: "the text stays in the field and the condition value remains empty"
        valueComponent.value == "abc"
        condition.value == null

        when: "the user invokes the 'Clear values' action"
        filter.getAction(GenericFilterClearValuesAction.ID).actionPerform(null)

        then: "the field is empty and the conversion error is gone"
        valueComponent.value == ""
        !condition.invalid
    }

    def "clear values clears an unconvertable input typed over an existing value"() {
        given: "a numeric condition with a value"
        def view = navigateToView(GfClearValuesTestView)
        GenericFilter filter = view.genericFilter
        PropertyFilter<BigDecimal> condition = view.amountCondition
        TypedTextField<BigDecimal> valueComponent = condition.valueComponent as TypedTextField<BigDecimal>
        condition.value = BigDecimal.valueOf(50)

        when: "the user replaces it with text that is not a number"
        typeFromClient(valueComponent, "abc")

        and: "the user invokes the 'Clear values' action"
        filter.getAction(GenericFilterClearValuesAction.ID).actionPerform(null)

        then: "the field no longer contains the text"
        valueComponent.value == ""
        condition.value == null
    }

    def "clear values clears a condition nested in a group"() {
        given: "a condition with a value inside a group"
        def view = navigateToView(GfClearValuesTestView)
        GenericFilter filter = view.genericFilter
        PropertyFilter<String> nestedCondition = view.nestedCondition

        expect: "the nested condition holds a value"
        nestedCondition.value == "n1"

        when: "the user invokes the 'Clear values' action"
        filter.getAction(GenericFilterClearValuesAction.ID).actionPerform(null)

        then: "the nested condition is empty"
        nestedCondition.value == null
    }

    def "clear values clears a condition nested two groups deep"() {
        given: "a numeric condition inside a group inside a group"
        def view = navigateToView(GfClearValuesTestView)
        GenericFilter filter = view.genericFilter
        PropertyFilter<BigDecimal> deepCondition = view.deepCondition
        TypedTextField<BigDecimal> valueComponent = deepCondition.valueComponent as TypedTextField<BigDecimal>

        and: "it holds a value and then gets an input that is not a number"
        deepCondition.value = BigDecimal.valueOf(10)
        typeFromClient(valueComponent, "abc")

        when: "the user invokes the 'Clear values' action"
        filter.getAction(GenericFilterClearValuesAction.ID).actionPerform(null)

        then: "the deeply nested condition is empty and shows no input"
        deepCondition.value == null
        valueComponent.value == ""
    }

    def "clear values clears an unparsable input in a date condition"() {
        given: "a date condition with an empty value"
        def view = navigateToView(GfClearValuesTestView)
        GenericFilter filter = view.genericFilter
        PropertyFilter<LocalDate> condition = view.dateCondition
        TypedDatePicker<LocalDate> valueComponent = condition.valueComponent as TypedDatePicker<LocalDate>

        when: "the user types text that is not a date"
        typeUnparsableFromClient(valueComponent, "abrakadabra")

        then: "the text stays in the input element and the condition is invalid"
        valueComponent.element.getProperty("_inputElementValue") == "abrakadabra"
        condition.value == null
        condition.invalid

        when: "the user invokes the 'Clear values' action"
        filter.getAction(GenericFilterClearValuesAction.ID).actionPerform(null)

        then: "the input element is empty and the error is gone"
        valueComponent.element.getProperty("_inputElementValue") == ""
        !condition.invalid
    }

    def "clearing an empty condition fires no value change event"() {
        given: "a numeric condition that is empty and holds no input"
        def view = navigateToView(GfClearValuesTestView)
        GenericFilter filter = view.genericFilter
        PropertyFilter<BigDecimal> condition = view.amountCondition

        and: "a listener recording value change events"
        def events = []
        condition.addValueChangeListener({ event -> events << event })

        when: "the user invokes the 'Clear values' action"
        filter.getAction(GenericFilterClearValuesAction.ID).actionPerform(null)

        then: "the value has not changed, so nothing is fired"
        events.isEmpty()
    }

    private static void typeFromClient(TypedTextField<?> field, String text) {
        field.element.node.getFeature(ElementPropertyMap)
                .deferredUpdateFromClient("value", text)
                .run()
    }

    private static void typeUnparsableFromClient(Component field, String text) {
        field.element.setProperty("_inputElementValue", text)
        field.element.node.getFeature(ElementListenerMap)
                .fireEvent(new DomEvent(field.element, "unparsable-change", JacksonUtils.createObjectNode()))
    }
}

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

package component_trim


import com.vaadin.flow.component.Component
import com.vaadin.flow.internal.nodefeature.ElementPropertyMap
import component_trim.view.ComponentTrimView
import io.jmix.core.Metadata
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.exception.ComponentValidationException
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.dataaware.TestLengthEntity
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ComponentTrimTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents

    @Autowired
    Metadata metadata

    @Override
    void setup() {
        registerViewBasePackages "component_trim.view"
    }

    def "TextArea trimming"() {
        given: "Opened view with TextArea component"
        def view = navigateToView ComponentTrimView
        def hasTrimmingComponent = view.textArea
        def valueForTrimming = "  value for trimming   "

        when: "Trimming is enabled for TextArea"
        // setting value from client
        hasTrimmingComponent.class.getDeclaredMethod('setModelValue', String.class, boolean.class)
                .tap {
                    accessible = true
                }.invoke hasTrimmingComponent, valueForTrimming, true

        then: "The value will be trimmed"
        valueForTrimming.trim().equals hasTrimmingComponent.value

        when: "Trimming is disabled for TextArea"
        hasTrimmingComponent.trimEnabled = false
        hasTrimmingComponent.setValue valueForTrimming

        then: "The value will not be trimmed"
        valueForTrimming.equals hasTrimmingComponent.value
    }

    def "TextArea fires a single value change event from the client for a trimmed value"() {
        given: "Opened view with TextArea component"
        def textArea = navigateToView(ComponentTrimView).textArea
        def events = []
        textArea.addValueChangeListener { events << it }

        when: "The client sends a value that needs trimming"
        setValueFromClient textArea, "test "

        then: "One event from the client with the trimmed value"
        events.size() == 1
        events[0].fromClient
        events[0].value == "test"
        textArea.value == "test"
        textArea.element.getProperty("value") == "test"
    }

    def "TextArea gives an empty value for blank input from the client"() {
        given: "TextArea with a value"
        def textArea = navigateToView(ComponentTrimView).textArea
        setValueFromClient textArea, "value"
        def events = []
        textArea.addValueChangeListener { events << it }

        when: "The client sends whitespace only"
        setValueFromClient textArea, "   "

        then: "One event from the client, the value is empty"
        events.size() == 1
        events[0].fromClient
        textArea.value == ""
        textArea.isEmpty()
        textArea.element.getProperty("value") == ""
    }

    def "Required TextArea cleared from the client is invalid"() {
        given: "Required TextArea with a value"
        def textArea = navigateToView(ComponentTrimView).textArea
        textArea.required = true
        setValueFromClient textArea, "value"

        when: "The client clears the field"
        setValueFromClient textArea, ""

        then: "The field is invalid"
        textArea.isInvalid()

        when: "Validators are executed"
        textArea.executeValidators()

        then: "The required check fails"
        thrown(ComponentValidationException)
    }

    def "Read-only TextArea ignores a value from the client"() {
        given: "Read-only TextArea with a value"
        def textArea = navigateToView(ComponentTrimView).textArea
        textArea.value = "value"
        textArea.readOnly = true
        def events = []
        textArea.addValueChangeListener { events << it }

        when: "The client sends a value that needs trimming"
        setValueFromClient textArea, "other "

        then: "The value is not changed"
        events.isEmpty()
        textArea.value == "value"
        textArea.element.getProperty("value") == "value"
    }

    def "TextArea bound to an attribute fires a single event for blank input from the client"() {
        given: "TextArea bound to an entity attribute with a value"
        def textArea = navigateToView(ComponentTrimView).textArea
        def container = dataComponents.createInstanceContainer(TestLengthEntity)
        def entity = metadata.create(TestLengthEntity)
        entity.lengthAttribute = "value"
        container.setItem(entity)
        textArea.valueSource = new ContainerValueSource<>(container, "lengthAttribute")
        def events = []
        textArea.addValueChangeListener { events << it }

        when: "The client sends whitespace only"
        setValueFromClient textArea, "   "

        then: "One event from the client, the attribute gets an empty string"
        events.size() == 1
        events[0].fromClient
        entity.lengthAttribute == ""
    }

    def "TextArea fires no event when the trimmed client value equals the current value"() {
        given: "TextArea with a value"
        def textArea = navigateToView(ComponentTrimView).textArea
        setValueFromClient textArea, "value"
        def events = []
        textArea.addValueChangeListener { events << it }

        when: "The client sends the same value with spaces"
        setValueFromClient textArea, "value  "

        then: "No event, the client shows the trimmed value"
        events.isEmpty()
        textArea.value == "value"
        textArea.element.getProperty("value") == "value"
    }

    def "TextArea does not trim a value set from code"() {
        given: "TextArea with trimming enabled"
        def textArea = navigateToView(ComponentTrimView).textArea
        textArea.trimEnabled = true

        when: "A value with spaces is set from code"
        textArea.value = " value "

        then: "The value is kept as is"
        textArea.value == " value "
        textArea.element.getProperty("value") == " value "
    }

    def "TextField fires value change events from the client for a trimmed value"() {
        given: "Opened view with TextField component"
        def textField = navigateToView(ComponentTrimView).textField
        def events = []
        def typedEvents = []
        textField.addValueChangeListener { events << it }
        textField.addTypedValueChangeListener { typedEvents << it }

        when: "The client sends a value that needs trimming"
        setValueFromClient textField, "test "

        then: "One event of each kind from the client with the trimmed value"
        events.size() == 1
        events[0].fromClient
        typedEvents.size() == 1
        typedEvents[0].fromClient
        textField.value == "test"
        textField.element.getProperty("value") == "test"
    }

    def "TextField gives an empty value for blank input from the client"() {
        given: "TextField with a value"
        def textField = navigateToView(ComponentTrimView).textField
        setValueFromClient textField, "value"

        when: "The client sends whitespace only"
        setValueFromClient textField, "   "

        then: "The value is empty, the typed value is null"
        textField.value == ""
        textField.typedValue == null
        textField.isEmpty()
    }

    // Applies the value the same way as the RPC handler does for a property synchronized from the client
    static void setValueFromClient(Component component, String value) {
        component.element.node.getFeature(ElementPropertyMap)
                .deferredUpdateFromClient("value", value)
                .run()
    }
}

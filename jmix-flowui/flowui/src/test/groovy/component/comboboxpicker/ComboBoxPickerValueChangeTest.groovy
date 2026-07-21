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

package component.comboboxpicker

import component.comboboxpicker.view.ComboBoxPickerTestView
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ComboBoxPickerValueChangeTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component.comboboxpicker.view")
    }

    def "value change events via setValueFromClient must be marked as from client"() {
        given: "A ComboBoxPicker with items"
        navigateToView(ComboBoxPickerTestView)
        def view = UiTestUtils.currentView as ComboBoxPickerTestView
        view.comboBoxPicker.setItems("a", "b")

        and: "A listener recording the fromClient flag of every value change event"
        def fromClientFlags = []
        view.comboBoxPicker.addValueChangeListener({ event -> fromClientFlags << event.fromClient })

        when: "User selects a value and then clears it"
        view.comboBoxPicker.setValueFromClient("a")
        view.comboBoxPicker.setValueFromClient(null)

        then: "An event was fired and every event is client-originated"
        !fromClientFlags.isEmpty()
        fromClientFlags.every { it }
    }

    def "clearing via setValueFromClient must not cause unparseable validation error"() {
        given: "A ComboBoxPicker with a pre-filled value"
        navigateToView(ComboBoxPickerTestView)
        def view = UiTestUtils.currentView as ComboBoxPickerTestView
        view.comboBoxPicker.setItems("a", "b")
        view.comboBoxPicker.value = "a"

        when: "User clears the value"
        view.comboBoxPicker.setValueFromClient(null)

        then: "The component must not be marked invalid (regression guard for #5164)"
        // base Vaadin ComboBox getErrorMessage() returns "" rather than null, so assert isInvalid()
        !view.comboBoxPicker.invalid
    }
}

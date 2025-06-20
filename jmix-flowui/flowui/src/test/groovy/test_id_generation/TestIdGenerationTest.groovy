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

package test_id_generation

import org.springframework.boot.test.context.SpringBootTest
import test_id_generation.view.TestIdGenerationView
import test_support.spec.FlowuiTestSpecification

import static io.jmix.flowui.sys.UiTestIdSupport.UI_TEST_ID

@SpringBootTest(properties = "jmix.ui.ui-test-mode=true")
class TestIdGenerationTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("test_id_generation.view")
    }

    def "Calculate UiTestId"() {
        given: "The view with components"
        def view = navigateToView TestIdGenerationView

        when: "UiTestId is calculated for a Button with an assigned action"
        def button_1 = view.button_1

        then: "ID must be calculated based on the action ID"
        button_1.element.getAttribute(UI_TEST_ID) == view.actionId.id

        when: "UiTestId is calculated for a Button with a text"
        def button_2 = view.button_2

        then: "ID must be calculated based on the text value"
        button_2.element.getAttribute(UI_TEST_ID) == "${button_2.text}JmixButton"

        when: "UiTestId is calculated for a TextField with a data binding"
        def textField_1 = view.textField_1
        def textField_2 = view.textField_2

        then: "ID must be calculated based on the data binding"
        textField_1.element.getAttribute(UI_TEST_ID) == "number"
        textField_2.element.getAttribute(UI_TEST_ID) == "user_name"

        when: "UiTestId is calculated for a TextField with a label"
        def textField_3 = view.textField_3

        then: "ID must be calculated based on the label value"
        textField_3.element.getAttribute(UI_TEST_ID) == "${textField_3.label}TypedTextField"

        when: "UiTextId is calculated for a ListDataComponent"
        def dataGrid = view.dataGrid

        then: "ID must be calculated based on the data binding"
        dataGrid.element.getAttribute(UI_TEST_ID) == "test_OrderDataGrid"
    }
}

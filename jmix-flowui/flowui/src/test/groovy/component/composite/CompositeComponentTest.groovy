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

package component.composite

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.icon.VaadinIcon
import component.composite.component.TestDataGridPanel
import component.composite.component.TestIncorrectStepperField
import component.composite.component.TestIncorrectTypedTextField
import component.composite.component.TestStepperField
import component.composite.component.TestTypedTextField
import io.jmix.flowui.UiComponents
import io.jmix.flowui.data.grid.EmptyDataGridItems
import io.jmix.flowui.exception.GuiDevelopmentException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.lang.Nullable
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class CompositeComponentTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    def "Composite component containing a DataGrid with MetaClass and relative path to descriptor"() {
        when:
        def dataGridPanel = uiComponents.create(TestDataGridPanel)

        then:
        noExceptionThrown()
        dataGridPanel.clicks == 0
        dataGridPanel.testBtn.text == "Test"
        getIconAttribute(dataGridPanel.testBtn.icon) == getIconAttribute(VaadinIcon.PLUS.create())
        dataGridPanel.items instanceof EmptyDataGridItems

        when:
        dataGridPanel.click()

        then:
        dataGridPanel.getClicks() == 1

        // TODO: gg, items
    }

    def "Composite component as field and full path to descriptor"() {
        when:
        def stepperField = uiComponents.create(TestStepperField)

        then:
        noExceptionThrown()
        stepperField.value == 0

        when:
        stepperField.clickUp()

        then:
        stepperField.value == 1

        when:
        stepperField.clickDown()

        then:
        stepperField.value == 0
    }

    def "Composite component with programmatic creation of nested components"() {
        when:
        def textField = uiComponents.create(TestTypedTextField)

        then:
        noExceptionThrown()
        textField.getContent().isClearButtonVisible()
    }

    def "Composite component with several root components in XML" () {
        when:
        def component = uiComponents.create(TestIncorrectStepperField)

        then:
        thrown(GuiDevelopmentException)
    }

    def "Composite component with incorrect root component type in XML"() {
        when:
        def component = uiComponents.create(TestIncorrectTypedTextField)

        then:
        thrown(GuiDevelopmentException)
    }

    @Nullable
    private static String getIconAttribute(Component icon) {
        return icon != null ? icon.element.getAttribute("icon") : null
    }
}

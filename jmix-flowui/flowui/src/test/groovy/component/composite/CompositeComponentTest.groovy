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
import component.composite.component.*
import component.composite.component.ext.TestStepperFieldExt
import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.data.grid.ContainerDataGridItems
import io.jmix.flowui.data.grid.EmptyDataGridItems
import io.jmix.flowui.exception.GuiDevelopmentException
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.lang.Nullable
import test_support.entity.sales.Product
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class CompositeComponentTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents
    @Autowired
    DataComponents dataComponents
    @Autowired
    Metadata metadata

    def "Composite component containing a DataGrid with MetaClass and relative path to descriptor"() {
        when:
        def dataGridPanel = uiComponents.create(TestDataGridPanel)

        then:
        dataGridPanel.clicks == 0
        dataGridPanel.testBtn.text == "Test"
        getIconAttribute(dataGridPanel.testBtn.icon) == getIconAttribute(VaadinIcon.PLUS.create())
        dataGridPanel.items instanceof EmptyDataGridItems

        when:
        dataGridPanel.click()

        then:
        dataGridPanel.getClicks() == 1

        when:
        def container = dataComponents.createCollectionContainer(Product)
        container.setItems(List.of(
                metadata.create(Product),
                metadata.create(Product)
        ))
        dataGridPanel.setDataContainer(container)

        then:
        dataGridPanel.items instanceof ContainerDataGridItems
        dataGridPanel.items.items.size() == 2
    }

    def "Composite component as field and full path to descriptor"() {
        when:
        def stepperField = uiComponents.create(TestStepperField)

        then:
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
        textField.getContent().isClearButtonVisible()
        !textField.postInitListenerFired
    }

    def "Composite component with several root components in XML"() {
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

    def "Extended Composite component inherits a full path XML descriptor"() {
        when:
        def stepperField = uiComponents.create(TestStepperFieldExt)

        then:
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

    def "Composite component with different components"() {
        when:
        def component = uiComponents.create(TestCompositeComponent)
        def tabSheet = component.tabSheet

        then: "TabSheet correctly loads tabs"
        tabSheet.getTabAt(0).getId().orElse("") == "tab1"
        tabSheet.getTabAt(0).getLabel() == "Tab 1"

        tabSheet.getTabAt(1).getId().orElse("") == "tab2"
        tabSheet.getTabAt(1).getLabel() == "Tab 2"

        when:
        def tabs = component.tabs

        then: "Tabs correctly loads tabs"
        tabs.getTabAt(0).getId().orElse("") == "tab1"
        tabs.getTabAt(0).getLabel() == "Tab 1"

        tabs.getTabAt(1).getId().orElse("") == "tab2"
        tabs.getTabAt(1).getLabel() == "Tab 2"

        when:
        def accordionPanel1 = component.accordionPanel1
        def accordionPanel2 = component.accordionPanel2

        then: "Accordion panels are found"
        accordionPanel1.summaryText == "Panel 1"
        accordionPanel2.summaryText == "Panel 2"

        when:
        def dropdownButton = component.dropdownButton
        def dropdownButtonItem1 = dropdownButton.getItem("componentItem")
        def dropdownButtonSubpart1 = dropdownButton.getSubPart("componentItem")
        def dropdownButtonItem2 = dropdownButton.getItem("textItem")
        def dropdownButtonSubpart2 = dropdownButton.getSubPart("textItem")

        then: "DropdownButton items can be found by id"
        dropdownButtonItem1 != null
        dropdownButtonSubpart1 != null
        dropdownButtonItem1 == dropdownButtonSubpart1

        dropdownButtonItem2 != null
        dropdownButtonSubpart2 != null
        dropdownButtonItem2 == dropdownButtonSubpart2
    }

    @Nullable
    private static String getIconAttribute(Component icon) {
        return icon != null ? icon.element.getAttribute("icon") : null
    }
}

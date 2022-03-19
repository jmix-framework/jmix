/*
 * Copyright 2021 Haulmont.
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

package components_helper

import component_registration.config.ComponentRegistrationTestConfiguration
import components_helper.screen.ComponentsHelperTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.ButtonsPanel
import io.jmix.ui.component.Component
import io.jmix.ui.component.ComponentVisitor
import io.jmix.ui.component.ComponentsHelper
import io.jmix.ui.component.Table
import io.jmix.ui.component.TextField
import io.jmix.ui.component.data.table.EmptyTableItems
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.petclinic.Country

@ContextConfiguration(classes = [CoreConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiConfiguration, UiTestConfiguration, ComponentRegistrationTestConfiguration])
class ComponentsHelperTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["components_helper.screen"])
    }

    def "walkComponents"() {
        showTestMainScreen()

        when: """
              Create screen and components tree.
              Walk through all components and save ids.
              """
        def screen = (ComponentsHelperTestScreen) screens.create(ComponentsHelperTestScreen)

        def visitedComponents = []
        ComponentsHelper.walkComponents(screen.window, new ComponentVisitor() {
            @Override
            void visit(Component component, String name) {
                visitedComponents.add(component.id)
            }
        })

        then: """
              Visited components should contain components from 
              HasComponent and HasInnerComponent inheritors.
              """

        def allComponents = ["table", "buttonsPanel", "textField",
                             "workArea", "initialLayout", "initialLayoutVbox",
                             "initialLayoutLabel", "initialLayoutDataGrid", "initialLayoutButtonsPanel"]

        visitedComponents == allComponents
    }

    def "focusChildComponent"() {
        showTestMainScreen()

        when: "Create screen and components tree. Focus child component from HasComponents inheritor."
        def screen = (ComponentsHelperTestScreen) screens.create(ComponentsHelperTestScreen)
        screen.show()

        // window implements HasComponents interface
        def focused = ComponentsHelper.focusChildComponent(screen.window)

        then: "The first component that implements Focusable will be focused."

        screen.table == focused

        when: "Configure components tree and focus component from HasInnerComponents inheritor."

        def table = (Table) uiComponents.create(Table)
        def buttonsPanel = (ButtonsPanel) uiComponents.create(ButtonsPanel)
        def textField = (TextField) uiComponents.create(TextField)

        buttonsPanel.add(textField)
        table.setButtonsPanel(buttonsPanel)
        table.setItems(new EmptyTableItems(metadata.getClass(Country)))

        // table implements HasInnerComponents interface
        focused = ComponentsHelper.focusChildComponent(table)

        then: "First component that implements Focusable from HasInnerComponents should be focused."

        textField == focused
    }
}

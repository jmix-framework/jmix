/*
 * Copyright 2023 Haulmont.
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

package component_xml_load

import com.vaadin.flow.component.shared.Tooltip
import com.vaadin.flow.data.value.ValueChangeMode
import component_xml_load.screen.MenuFilterFieldView
import io.jmix.flowui.component.menufilterfield.MenuFilterField
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(["jmix.ui.composite-menu=false", "jmix.ui.menu-config=menu/menufilterfield/menu.xml"])
class MenuFilterFieldXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load MenuFilterField component from XML"() {
        when: "Open the MenuFilterFieldView"
        def menuFilterFieldView = navigateToView(MenuFilterFieldView.class)

        then: "MenuFilterField attributes will be loaded"
        verifyAll(menuFilterFieldView.menuFilterField) {
            id.get() == "menuFilterField"
            autofocus
            autoselect
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            clearButtonVisible
            enabled
            height == "50px"
            helperText == "helperTextString"
            label == "labelString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            placeholder == "placeholderString"
            readOnly
            filterMode == MenuFilterField.FilterMode.CASE_SENSITIVE
            tabIndex == 3
            themeNames.containsAll(["small", "align-right"])
            title == "titleString"
            valueChangeMode == ValueChangeMode.ON_CHANGE
            valueChangeTimeout == 50
            visible
            width == "100px"
            menuItemProvider == menuFilterFieldView.listMenu.menuItemProvider

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }
}

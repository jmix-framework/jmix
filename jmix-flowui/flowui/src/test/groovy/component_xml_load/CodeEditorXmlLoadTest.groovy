/*
 * Copyright 2022 Haulmont.
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
import component_xml_load.screen.ComponentView
import io.jmix.core.DataManager
import io.jmix.flowui.kit.component.codeeditor.CodeEditorMode
import io.jmix.flowui.kit.component.codeeditor.CodeEditorTheme
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class CodeEditorXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def order = dataManager.create(Order)
        order.number = "textFieldValue"

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load codeEditor component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)
        componentView.loadData()

        then: "CodeEditor attributes will be loaded"
        verifyAll(componentView.codeEditorId) {
            id.get() == "codeEditorId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            errorMessage == "errorMessageString"
            fontSize == "20"
            height == "50px"
            helperText == "helperTextString"
            !invalid
            label == "labelString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            mode == CodeEditorMode.SQL
            printMarginColumn == 120
            readOnly
            required
            requiredIndicatorVisible
            requiredMessage == "requiredMessageString"
            !showGutter
            !showLineNumbers
            !showPrintMargin
            tabIndex == 3
            theme == CodeEditorTheme.TERMINAL
            title == "titleString"
            !visible
            width == "100px"

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM

            value == dataManager.load(Order).all().one().number
        }
    }
}

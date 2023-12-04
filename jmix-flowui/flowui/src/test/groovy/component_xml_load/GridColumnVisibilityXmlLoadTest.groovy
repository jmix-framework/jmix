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

import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.icon.VaadinIcon
import component_xml_load.screen.GridColumnVisibilityView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

import java.time.LocalDate

@SpringBootTest
class GridColumnVisibilityXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def order = dataManager.create(Order)
        order.number = "number"
        order.date = LocalDate.now()
        order.amount = BigDecimal.valueOf(5)

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        //noinspection SqlDialectInspection, SqlNoDataSourceInspection
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load gridColumnVisibility component from XML"() {
        when: "Open the GridColumnVisibilityView"

        def columnVisibilityView = navigateToView(GridColumnVisibilityView.class)

        then: "gridColumnVisibility attributes are loaded"
        verifyAll(columnVisibilityView.columnVisibility) {
            id.get() == "columnVisibility"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            minHeight == "1em"
            minWidth == "2em"
            height == "3em"
            width == "4em"
            maxHeight == "5em"
            maxWidth == "6em"
            tabIndex == 4
            !visible
            grid == columnVisibilityView.dataGrid
            themeNames.containsAll(["tertiary", "primary"])
            text == "Show/hide columns"
            icon.element.getAttribute("icon") ==
                    VaadinIcon.COG.create().element.getAttribute("icon")
            !hideAllEnabled
            showAllEnabled
            whiteSpace == HasText.WhiteSpace.NOWRAP
            getMenuItems().size() == 5
            getMenuItem("number") != null
            getMenuItem("number").checked
            getMenuItem("number").text == "Number"
            getMenuItem("dateTime") != null
            getMenuItem("total") != null
            !getMenuItem("total").checked
            getMenuItem("total").text == "overall"
            getMenuItem("amountColumn") != null
            getMenuItem("generated") != null
        }
    }

    def "Load gridColumnVisibility with include attribute from XML"() {
        when: "Open the GridColumnVisibilityView"

        def columnVisibilityView = navigateToView(GridColumnVisibilityView.class)

        then: "gridColumnVisibility include attribute is loaded"
        verifyAll(columnVisibilityView.anotherColumnVisibility) {
            id.get() == "anotherColumnVisibility"
            getMenuItems().size() == 2
            getMenuItem("number") != null
            getMenuItem("total") != null
        }
    }
}

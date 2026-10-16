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

package component.grid

import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.component.grid.TreeDataGrid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification
import tools.jackson.databind.node.ObjectNode

@SpringBootTest
class DataGridI18nTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    def "#gridClass.simpleName takes the accessible names from messages"() {
        when: "A grid is created"
        def grid = uiComponents.create(gridClass)

        then: "The accessible names are the localized defaults, with the placeholders kept for the client"
        grid.i18n.selectAll == "Select All"
        grid.i18n.selectAllUnavailable == "Select All unavailable"
        grid.i18n.selectRow == "Select Row {rowHeader}"
        grid.i18n.sorter == "Sort by {column}"

        and: "The localized defaults are sent to the client"
        def i18nProperty = grid.element.getPropertyRaw("i18n") as ObjectNode
        i18nProperty.get("selectAll").asString() == "Select All"
        i18nProperty.get("selectAllUnavailable").asString() == "Select All unavailable"
        i18nProperty.get("selectRow").asString() == "Select Row {rowHeader}"
        i18nProperty.get("sorter").asString() == "Sort by {column}"

        where:
        gridClass << [DataGrid, TreeDataGrid]
    }
}

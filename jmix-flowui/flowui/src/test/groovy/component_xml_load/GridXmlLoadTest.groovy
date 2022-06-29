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

import com.vaadin.flow.component.grid.ColumnTextAlign
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.dnd.GridDropMode
import component_xml_load.screen.GridView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

import java.time.LocalDate

@SpringBootTest
class GridXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")

        def order = dataManager.create(Order)
        order.number = "number"
        order.date = LocalDate.now()
        order.amount = BigDecimal.valueOf(5)

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load dataGrid component from XML"() {
        given: "Screen with a dataGrid"
        def gridView = openScreen(GridView.class)
        gridView.loadData()

        when: "dataGrid is loaded"
        def dataGrid = gridView.dataGrid

        then: "dataGrid attributes are loaded"
        verifyAll(dataGrid) {
            id.get() == "dataGrid"
            allRowsVisible
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            columnReorderingAllowed
            detailsVisibleOnClick
            dropMode == GridDropMode.BETWEEN
            enabled
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            multiSort
            nestedNullBehavior == Grid.NestedNullBehavior.THROW
            pageSize == 20
            rowsDraggable
            themeNames.containsAll(["column-borders", "compact"])
            verticalScrollingEnabled
            visible
            width == "100px"
            columns.size() == 11
        }

        when: "anotherDataGrid is loaded"
        def anotherDataGrid = gridView.anotherDataGrid

        then: "anotherDataGrid attributes are loaded"
        verifyAll(anotherDataGrid) {
            columns.size() == 3
            actions.find().id == "gridAction"
        }

        verifyAll(anotherDataGrid.getColumnByKey("number")) {
            key == "number"
            autoWidth
            flexGrow == 5
            !frozen
            resizable
            sortable
            textAlign == ColumnTextAlign.END
            visible
            width == "100px"
        }

        when: "metaClassDataGrid is loaded"
        def metaClassDataGrid = gridView.metaClassDataGrid

        then: "metaClassDataGrid attributes are loaded"
        metaClassDataGrid.columns.size() == 2
    }

    def "Load treeDataGrid component from XML"() {
        given: "Screen with a treeDataGrid"
        def gridView = openScreen(GridView.class)
        gridView.loadData()

        when: "treeDataGrid is loaded"
        def treeDataGrid = gridView.treeDataGrid

        then: "treeDataGrid attributes are loaded"
        verifyAll(treeDataGrid) {
            id.get() == "treeDataGrid"
            allRowsVisible
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            columnReorderingAllowed
            detailsVisibleOnClick
            dropMode == GridDropMode.BETWEEN
            enabled
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            multiSort
            nestedNullBehavior == Grid.NestedNullBehavior.THROW
            pageSize == 20
            rowsDraggable
            themeNames.containsAll(["column-borders", "compact"])
            verticalScrollingEnabled
            visible
            width == "100px"
            columns.size() == 11
        }

        when: "anotherTreeDataGrid is loaded"
        def anotherTreeDataGrid = gridView.anotherTreeDataGrid

        then: "anotherTreeDataGrid attributes are loaded"
        verifyAll(anotherTreeDataGrid) {
            columns.size() == 3
            actions.find().id == "gridAction"
        }

        verifyAll(anotherTreeDataGrid.getColumnByKey("number")) {
            key == "number"
            autoWidth
            flexGrow == 5
            !frozen
            resizable
            sortable
            textAlign == ColumnTextAlign.END
            visible
            width == "100px"
        }

        when: "metaClassTreeDataGrid is loaded"
        def metaClassTreeDataGrid = gridView.metaClassTreeDataGrid

        then: "metaClassTreeDataGrid attributes are loaded"
        metaClassTreeDataGrid.columns.size() == 2
    }
}

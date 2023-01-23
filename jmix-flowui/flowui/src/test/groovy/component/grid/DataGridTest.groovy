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

package component.grid

import component.grid.view.DataGridTestView
import io.jmix.flowui.Actions
import io.jmix.flowui.UiComponents
import io.jmix.flowui.action.list.CreateAction
import io.jmix.flowui.action.list.EditAction
import io.jmix.flowui.action.list.RemoveAction
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.kit.action.BaseAction
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DataGridTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents
    @Autowired
    Actions actions

    @Override
    void setup() {
        registerScreenBasePackages("component.grid")
    }

    def "Add actions without text"() {
        def dataGrid = uiComponents.create(DataGrid)

        def createAction = actions.create(CreateAction)
        def editAction = actions.create(EditAction)
        def notTextAction = new BaseAction("noText")
        def removeAction = actions.create(RemoveAction)

        when:
        dataGrid.addAction(createAction)
        dataGrid.addAction(editAction)
        dataGrid.addAction(notTextAction)
        dataGrid.addAction(removeAction)

        then:
        noExceptionThrown()
    }

    def "Load DataGrid without columns in XML"() {
        when: "Open View with DatGrid without columns"
        def screen = openScreen(DataGridTestView)

        then: "DataGrid should be loaded without columns"

        screen.dataGridWithoutColumns.columns.isEmpty()
    }

    def "Move columns in DataGrid"() {
        when: """
              Columns in DataGrid has the following order:
              |number|date|dateTime|time|amount|
              |  0   | 1  |   2    | 3  |  4   |
              
              Change "number" position to 3. 
              """
        def screen = openScreen(DataGridTestView)

        def numberColumn = screen.dataGridMoveColumns.getColumnByKey("number")
        screen.dataGridMoveColumns.setColumnPosition(numberColumn, 3)

        then: """
              Columns should be in the following order:
              |date|dateTime|time|number|amount|
              | 0  |   1    | 2  |  3   |  4   |
              """

        screen.dataGridMoveColumns.allColumns.get(0).key == "date"
        screen.dataGridMoveColumns.allColumns.get(1).key == "dateTime"
        screen.dataGridMoveColumns.allColumns.get(2).key == "time"
        screen.dataGridMoveColumns.allColumns.get(3).key == "number"
        screen.dataGridMoveColumns.allColumns.get(4).key == "amount"

        when: """
             Index to move is equal to columns size
             """
        screen.dataGridMoveColumns.setColumnPosition(numberColumn, 5)

        then: """
              Exception should be thrown.
              """

        thrown(IndexOutOfBoundsException)
    }
}

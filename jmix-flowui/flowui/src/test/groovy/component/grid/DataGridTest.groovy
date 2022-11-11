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
}

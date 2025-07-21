/*
 * Copyright 2025 Haulmont.
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

import com.vaadin.flow.component.html.NativeLabel
import com.vaadin.flow.data.provider.Query
import component_xml_load.screen.GridLayoutView
import io.jmix.core.DataManager
import io.jmix.core.SaveContext
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.data.items.ContainerDataProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class GridLayoutXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def saveContext = new SaveContext()
        def order = dataManager.create(Order)
        def order1 = dataManager.create(Order)

        saveContext.saving(order, order1)
        dataManager.save(saveContext)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load GridLayout component from XML"() {
        when: "Open the GridLayoutView"
        def gridLayoutView = navigateToView(GridLayoutView)

        then: "GridLayout attributes will be loaded"
        verifyAll(gridLayoutView.gridLayoutId) {
            id.get() == "gridLayoutId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            height == "50px"
            maxHeight == "55px"
            columnMinWidth == "5em"
            gap == "var(--lumo-space-m)"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            visible
            width == "100px"

            children.count() == 2
            children.find { (it.id.orElse("") == "gridLayoutChild1") } instanceof NativeLabel
            children.find { (it.id.orElse("") == "gridLayoutChild2") } instanceof TypedTextField
        }
    }

    def "Load GridLayout component with binding from XML"() {
        when: "Open the GridLayoutView"
        def gridLayoutView = navigateToView(GridLayoutView)

        then: "GridLayout binding will be loaded"
        verifyAll(gridLayoutView.gridLayoutBindingId) {
            id.get() == "gridLayoutBindingId"
            dataProvider instanceof ContainerDataProvider
            (dataProvider as ContainerDataProvider).size(new Query()) == gridLayoutView.ordersDc.items.size()
        }
    }
}

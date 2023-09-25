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

package component.grid

import component.grid.view.DataGridAggregationTestView
import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.AggregationInfo
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.data.grid.ContainerDataGridItems
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.AggregationTestEntity
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DataGridAggregationTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata
    @Autowired
    DataComponents dataComponents

    @Override
    void setup() {
        registerViewBasePackages("component.grid")
    }

    def "Aggregate values"() {
        def view = navigateToView(DataGridAggregationTestView)

        def dataGrid = view.aggregationTopDataGrid
        setDataGridItems(dataGrid)

        when: "Aggregating values"
        def aggregationResults = dataGrid.getAggregationResults()
        then: "Aggregation results should be calculated"
        aggregationResults.get(dataGrid.getColumnByKey("name")) == 2        // count
        aggregationResults.get(dataGrid.getColumnByKey("count")) == 20      // min
        aggregationResults.get(dataGrid.getColumnByKey("price")) == 105.5   // avg
        aggregationResults.get(dataGrid.getColumnByKey("sales")) == 20      // max
        aggregationResults.get(dataGrid.getColumnByKey("usages")) == 76.0   // sum
    }

    def "Update aggregated values"() {
        def dataGrid = uiComponents.create(DataGrid)
        addAggregatedColumn(dataGrid, "count", AggregationInfo.Type.MIN)
        addAggregatedColumn(dataGrid, "price", AggregationInfo.Type.AVG)
        addAggregatedColumn(dataGrid, "sales", AggregationInfo.Type.MAX)
        addAggregatedColumn(dataGrid, "usages", AggregationInfo.Type.SUM)

        dataGrid.aggregatable = true
        setDataGridItems(dataGrid)

        when: "Updating values in container"
        def container = (dataGrid.getItems() as ContainerDataGridItems<AggregationTestEntity>).getContainer()
        def entity = container.getMutableItems().find({ it.name == "test1" })
        entity.setCount(5l)
        entity.setSales(10)
        entity.setPrice(105.5)
        entity.setUsages(15.5)

        then: "Aggregation values should be updated"
        def aggregationResults = dataGrid.getAggregationResults()
        aggregationResults.get(dataGrid.getColumnByKey("count")) == 5l  // min
        aggregationResults.get(dataGrid.getColumnByKey("price")) == 98d // avg
        aggregationResults.get(dataGrid.getColumnByKey("sales")) == 20  // max
        aggregationResults.get(dataGrid.getColumnByKey("usages")) == 66 // sum
    }

    def addAggregatedColumn(DataGrid dataGrid, String id, AggregationInfo.Type type) {
        def metaClass = metadata.getClass(AggregationTestEntity)

        def mpp = metaClass.getPropertyPath(id)
        dataGrid.addColumn(id, mpp)

        def aggregationInfo = new AggregationInfo()
        aggregationInfo.setPropertyPath(mpp)
        aggregationInfo.setType(type)

        dataGrid.addAggregation(dataGrid.getColumnByKey(id), aggregationInfo)
    }

    def setDataGridItems(DataGrid dataGrid) {
        def container = dataComponents.createCollectionContainer(AggregationTestEntity)
        def test1 = new AggregationTestEntity(name: 'test1', count: 20l, sales: 15, price: 120.5, usages: 25.5)
        def test2 = new AggregationTestEntity(name: 'test2', count: 25l, sales: 20, price: 90.5, usages: 50.5)

        container.items = [test1, test2]
        //noinspection GroovyAssignabilityCheck
        dataGrid.items = new ContainerDataGridItems(container)
    }
}

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

import com.vaadin.flow.component.grid.GridSortOrder
import component.grid.view.DataGridSortBuilderDelegateTestView
import io.jmix.core.DataManager
import io.jmix.core.Sort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.datastores.MainDsEntity
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DataGridSortBuilderDelegateTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager
    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component.grid.view")

        def first = dataManager.create(MainDsEntity)
        first.setName("b")
        first.setMem1DtoEntityId(UUID.randomUUID())

        def second = dataManager.create(MainDsEntity)
        second.setName("a")
        second.setMem1DtoEntityId(UUID.randomUUID())

        dataManager.save(first, second)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_MAIN_DS_ENTITY")
    }

    def "SortBuilderDelegate: JPA entity sorts by regular JPA property"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        view.setupJpaSortBuilderDelegate()
        def dataGrid = view.jpaDataGridPersistent

        when:
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("name")).build())

        then:
        def sort = view.mainDsEntitiesDl2.sort
        sort != null
        sort.orders*.property == ["name"]
        sort.orders*.direction == [Sort.Direction.ASC]
        !(sort.orders[0] instanceof Sort.ExpressionOrder)
    }

    def "SortBuilderDelegate: transient property uses comparator for in-memory sorting"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        view.setupJpaTransientInMemorySortBuilderDelegate()
        def dataGrid = view.jpaDataGridInMemory

        when:
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("mem1DtoEntity")).build())

        then:
        def sort = view.mainDsEntitiesDl1.sort
        sort != null
        sort.orders*.property == ["mem1DtoEntity"]
        sort.orders*.direction == [Sort.Direction.ASC]
        view.jpaTransientComparatorCallCount > 0
    }

    def "SortBuilderDelegate: transient property uses expression for DB sorting"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        view.setupJpaSortBuilderDelegate()
        def dataGrid = view.jpaDataGridPersistent

        when:
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("mem1DtoEntity")).build())

        then:
        def sort = view.mainDsEntitiesDl2.sort
        sort != null
        sort.orders.size() == 1
        sort.orders[0] instanceof Sort.ExpressionOrder
        sort.orders[0].expression == "e.mem1DtoEntityId"
        sort.orders[0].direction == Sort.Direction.ASC
    }

    def "SortBuilderDelegate: KeyValueEntity existing query column uses in-memory comparator"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        view.setupKeyValueInMemorySortBuilderDelegate()
        def dataGrid = view.keyValueDataGridInMemory

        when:
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("name")).build())

        then:
        def sort = view.keyValueEntitiesDl1.sort
        sort != null
        sort.orders*.property == ["name"]
        sort.orders*.direction == [Sort.Direction.ASC]
        view.keyValueComparatorCallCount > 0
    }

    def "SortBuilderDelegate: KeyValueEntity DB sorting accepts expression by non-selected attribute"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        view.setupKeyValuePersistentSortBuilderDelegate()
        def dataGrid = view.keyValueDataGridPersistent

        when:
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("name")).build())

        then:
        def sort = view.keyValueEntitiesDl2.sort
        sort != null
        sort.orders.size() == 1
        sort.orders[0] instanceof Sort.ExpressionOrder
        sort.orders[0].expression == "e.mem1DtoEntityId"
        sort.orders[0].direction == Sort.Direction.ASC
    }
}

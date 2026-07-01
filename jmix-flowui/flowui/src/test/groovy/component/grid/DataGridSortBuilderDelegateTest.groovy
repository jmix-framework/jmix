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
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.model.CollectionContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Unroll
import test_support.entity.datastores.MainDsEntity
import test_support.spec.FlowuiTestSpecification

import static component.grid.DataGridSortBuilderDelegateTest.Outcome.SUPPORTED
import static component.grid.DataGridSortBuilderDelegateTest.Outcome.UNSUPPORTED
import static component.grid.DataGridSortBuilderDelegateTest.PropertyType.GENERATED
import static component.grid.DataGridSortBuilderDelegateTest.PropertyType.META
import static component.grid.DataGridSortBuilderDelegateTest.PropertyType.TRANSIENT
import static component.grid.DataGridSortBuilderDelegateTest.ReplacementType.COLUMN_COMPARATOR
import static component.grid.DataGridSortBuilderDelegateTest.ReplacementType.DELEGATE_COMPARATOR
import static component.grid.DataGridSortBuilderDelegateTest.ReplacementType.DELEGATE_EXPRESSION
import static component.grid.DataGridSortBuilderDelegateTest.ReplacementType.NONE
import static component.grid.DataGridSortBuilderDelegateTest.SortMode.DATABASE
import static component.grid.DataGridSortBuilderDelegateTest.SortMode.IN_MEMORY

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
        first.setId(UUID.fromString("00000000-0000-0000-0000-000000000003"))
        first.setName("c")
        first.setMem1DtoEntityId(UUID.randomUUID())

        def second = dataManager.create(MainDsEntity)
        second.setId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
        second.setName("a")
        second.setMem1DtoEntityId(UUID.randomUUID())

        def third = dataManager.create(MainDsEntity)
        third.setId(UUID.fromString("00000000-0000-0000-0000-000000000002"))
        third.setName("b")
        third.setMem1DtoEntityId(UUID.randomUUID())

        dataManager.save(first, second, third)
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

    @Unroll
    def "SortBuilderDelegate matrix: #scenarioId"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        def dataGrid = (sortMode == IN_MEMORY ? view.jpaDataGridInMemory : view.jpaDataGridPersistent) as DataGrid<MainDsEntity>
        def container = (sortMode == IN_MEMORY ? view.mainDsEntitiesDc1 : view.mainDsEntitiesDc2) as CollectionContainer<MainDsEntity>

        view.setupTransientValues()

        if (replacementType == COLUMN_COMPARATOR) {
            view.setupColumnComparator(dataGrid, propertyType.columnKey)
        }

        if (hasDelegate) {
            view.setupJpaSortBuilderDelegate(
                    dataGrid,
                    propertyType.columnKey,
                    replacementType == DELEGATE_COMPARATOR,
                    replacementType == DELEGATE_EXPRESSION)
        }

        and:
        def beforeNames = container.items*.name

        when:
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey(propertyType.columnKey)).build())

        then:
        def afterNames = container.items*.name
        if (outcome == SUPPORTED) {
            afterNames == expectedOrder
        } else {
            afterNames == beforeNames
        }

        where:
        scenarioId                                  | propertyType | hasDelegate | sortMode  | replacementType     | outcome     | expectedOrder
        "MPP no delegate in-memory"                 | META         | false       | IN_MEMORY | NONE                | SUPPORTED   | ["a", "b", "c"]
        "MPP no delegate in-memory col comp"        | META         | false       | IN_MEMORY | COLUMN_COMPARATOR   | SUPPORTED   | ["c", "b", "a"]
        "MPP no delegate db"                        | META         | false       | DATABASE  | NONE                | SUPPORTED   | ["a", "b", "c"]
        "MPP delegate in-memory"                    | META         | true        | IN_MEMORY | NONE                | SUPPORTED   | ["a", "b", "c"]
        "MPP delegate in-memory col comp"           | META         | true        | IN_MEMORY | COLUMN_COMPARATOR   | SUPPORTED   | ["c", "b", "a"]
        "MPP delegate in-memory del comp"           | META         | true        | IN_MEMORY | DELEGATE_COMPARATOR | SUPPORTED   | ["c", "b", "a"]
        "MPP delegate db"                           | META         | true        | DATABASE  | NONE                | SUPPORTED   | ["a", "b", "c"]
        "MPP delegate db expr"                      | META         | true        | DATABASE  | DELEGATE_EXPRESSION | SUPPORTED   | ["a", "b", "c"]

        "Transient no delegate in-memory"           | TRANSIENT    | false       | IN_MEMORY | NONE                | SUPPORTED   | ["a", "b", "c"]
        "Transient no delegate in-memory col comp"  | TRANSIENT    | false       | IN_MEMORY | COLUMN_COMPARATOR   | SUPPORTED   | ["c", "b", "a"]
        "Transient no delegate db"                  | TRANSIENT    | false       | DATABASE  | NONE                | UNSUPPORTED | null
        "Transient delegate in-memory"              | TRANSIENT    | true        | IN_MEMORY | NONE                | SUPPORTED   | ["a", "b", "c"]
        "Transient delegate in-memory col comp"     | TRANSIENT    | true        | IN_MEMORY | COLUMN_COMPARATOR   | SUPPORTED   | ["c", "b", "a"]
        "Transient delegate in-memory del comp"     | TRANSIENT    | true        | IN_MEMORY | DELEGATE_COMPARATOR | SUPPORTED   | ["c", "b", "a"]
        "Transient delegate db"                     | TRANSIENT    | true        | DATABASE  | NONE                | UNSUPPORTED | null
        "Transient delegate db expr"                | TRANSIENT    | true        | DATABASE  | DELEGATE_EXPRESSION | SUPPORTED   | ["a", "b", "c"]

        "Generated no delegate in-memory"           | GENERATED    | false       | IN_MEMORY | NONE                | UNSUPPORTED | null
        "Generated no delegate in-memory col comp"  | GENERATED    | false       | IN_MEMORY | COLUMN_COMPARATOR   | SUPPORTED   | ["c", "b", "a"]
        "Generated no delegate db"                  | GENERATED    | false       | DATABASE  | NONE                | UNSUPPORTED | null
        "Generated delegate in-memory"              | GENERATED    | true        | IN_MEMORY | NONE                | UNSUPPORTED | null
        "Generated delegate in-memory col comp"     | GENERATED    | true        | IN_MEMORY | COLUMN_COMPARATOR   | SUPPORTED   | ["c", "b", "a"]
        "Generated delegate in-memory del comp"     | GENERATED    | true        | IN_MEMORY | DELEGATE_COMPARATOR | SUPPORTED   | ["c", "b", "a"]
        "Generated delegate db"                     | GENERATED    | true        | DATABASE  | NONE                | UNSUPPORTED | null
        "Generated delegate db expr"                | GENERATED    | true        | DATABASE  | DELEGATE_EXPRESSION | SUPPORTED   | ["a", "b", "c"]
    }

    def "SortBuilderDelegate: generated column descending sort direction is preserved with column comparator"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        def dataGrid = view.jpaDataGridInMemory as DataGrid<MainDsEntity>

        view.setupColumnComparator(dataGrid, "noPropertyColumn")

        when:
        dataGrid.sort(GridSortOrder.desc(dataGrid.getColumnByKey("noPropertyColumn")).build())

        then:
        view.mainDsEntitiesDc1.items*.name == ["a", "b", "c"]
    }

    def "SortBuilderDelegate: generated column descending sort direction is preserved with delegate comparator"() {
        given:
        def view = navigateToView(DataGridSortBuilderDelegateTestView)
        def dataGrid = view.jpaDataGridInMemory as DataGrid<MainDsEntity>

        view.setupJpaSortBuilderDelegate(dataGrid, "noPropertyColumn", true, false)

        when:
        dataGrid.sort(GridSortOrder.desc(dataGrid.getColumnByKey("noPropertyColumn")).build())

        then:
        view.mainDsEntitiesDc1.items*.name == ["a", "b", "c"]
    }

    private enum SortMode {
        IN_MEMORY,
        DATABASE
    }

    private enum PropertyType {
        META("name"),
        TRANSIENT("mem1DtoEntity"),
        GENERATED("noPropertyColumn")

        final String columnKey

        PropertyType(String columnKey) {
            this.columnKey = columnKey
        }
    }

    private enum ReplacementType {
        NONE,
        COLUMN_COMPARATOR,
        DELEGATE_COMPARATOR,
        DELEGATE_EXPRESSION
    }

    private enum Outcome {
        SUPPORTED,
        UNSUPPORTED
    }
}

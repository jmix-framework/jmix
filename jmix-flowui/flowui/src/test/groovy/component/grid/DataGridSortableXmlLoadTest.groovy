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
import component.grid.view.DataGridSortableXmlLoadTestView
import io.jmix.core.DataManager
import io.jmix.core.Sort
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.datastores.MainDsEntity
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DataGridSortableXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager
    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component.grid.view")

        2.times { dataManager.save(dataManager.create(MainDsEntity)) }
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_MAIN_DS_ENTITY")
    }

    def "DataGrid sets sortable for JPA column, sets non-sortable for Transient and No property columns"() {
        given:
        def view = navigateToView(DataGridSortableXmlLoadTestView)

        expect:
        view.entityDataGrid.getColumnByKey("name").sortable
        !view.entityDataGrid.getColumnByKey("mem1DtoEntity").sortable
        !view.entityDataGrid.getColumnByKey("noPropertyColumn").sortable
    }

    def "DataGrid sets sortable for DTO attributes"() {
        given:
        def view = navigateToView(DataGridSortableXmlLoadTestView)

        expect:
        view.dtoDataGrid.getColumnByKey("id").sortable
        view.dtoDataGrid.getColumnByKey("name").sortable
    }

    def "Non JPA columns are not included to in-memory sorting without comparators"() {
        given:
        def view = navigateToView(DataGridSortableXmlLoadTestView)
        def dataGrid = view.dataGridInMemory

        and:
        dataGrid.getColumnByKey("mem1DtoEntity").sortable
        dataGrid.getColumnByKey("noPropertyColumn").sortable

        when: "Sort by JPA column"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("name")).build())

        then: "Sort should be applied"
        def sort = view.mainDsEntitiesDl1.sort
        sort != null
        sort.orders*.property == ["name"]
        sort.orders*.direction == [Sort.Direction.ASC]
        dataGrid.sort([])

        when: "Sort by transient property"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("mem1DtoEntity")).build())

        then: """
              Sort should be applied, because the value of transient property can be sorted
              by default comparator from a Sorter.
              """
        def sort1 = view.mainDsEntitiesDl1.sort
        sort1 != null
        sort1.orders*.property == ["mem1DtoEntity"]
        sort1.orders*.direction == [Sort.Direction.ASC]
        dataGrid.sort([])

        when: "Sort by column without property"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("noPropertyColumn")).build())

        then: "Sort should not be applied"
        view.mainDsEntitiesDl1.sort == null

        when: "Set comparators for non-JPA columns"
        view.setupComparatorForNoPropertyColumn()
        view.setupComparatorForTransientPropertyColumn()

        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("name")).build())

        then: "JPA column can still be sorted"
        def sort2 = view.mainDsEntitiesDl1.sort
        sort2 != null
        sort2.orders*.property == ["name"]
        sort2.orders*.direction == [Sort.Direction.ASC]
        dataGrid.sort([])

        when: "Sort by transient property with Column comparator"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("mem1DtoEntity")).build())

        then: "Transient property should be included to in-memory sorting"
        def sort3 = view.mainDsEntitiesDl1.sort
        sort3 != null
        sort3.orders*.property == ["mem1DtoEntity"]
        sort3.orders*.direction == [Sort.Direction.ASC]
        dataGrid.sort([])

        when: "Sort by column without property with column comparator (in-memory sorting)"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("noPropertyColumn")).build())

        then: "Column without property should be included to sorting"
        def sort4 = view.mainDsEntitiesDl1.sort
        sort4 != null
        sort4.orders*.property == ["noPropertyColumn"]
        sort4.orders*.direction == [Sort.Direction.ASC]
    }

    def "Non JPA columns are not included to persistent sorting"() {
        given:
        def view = navigateToView(DataGridSortableXmlLoadTestView)
        def dataGrid = view.dataGridPersistent

        and: "Enable explicit comparators for non-JPA columns"
        view.setupComparatorForPersistentNoPropertyColumn()
        view.setupComparatorForPersistentTransientPropertyColumn()
        dataGrid.getColumnByKey("mem1DtoEntity").sortable
        dataGrid.getColumnByKey("noPropertyColumn").sortable

        when: "Sort by persistent property"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("name")).build())

        then: "Persistent sorting contains only JPA property"
        def sort1 = view.mainDsEntitiesDl2.sort
        sort1 != null
        sort1.orders*.property == ["name"]
        sort1.orders*.direction == [Sort.Direction.ASC]
        dataGrid.sort([])

        when: "Sort by transient property with comparator"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("mem1DtoEntity")).build())

        then: """
              Transient property must be included to persistent sorting, because there can be
              custom JpqlSortExpressionProvider.
              """
        view.mainDsEntitiesDl2.sort != null
        dataGrid.sort([])

        when: "Sort by column without property with comparator"
        dataGrid.sort(GridSortOrder.asc(dataGrid.getColumnByKey("noPropertyColumn")).build())

        then: "Column without property is not included into DB sorting"
        view.mainDsEntitiesDl2.sort == null
    }
}

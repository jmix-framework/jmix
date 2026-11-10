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

import com.vaadin.flow.component.HasText
import component.grid.view.DataGridAggregationTopHeaderTestView
import io.jmix.core.Metadata
import io.jmix.core.MessageTools
import io.jmix.flowui.component.grid.DataGridColumn
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.AggregationTestEntity
import test_support.entity.sec.Group
import test_support.spec.FlowuiTestSpecification

/**
 * With {@code aggregationPosition="TOP"} the aggregation row is the innermost header row, so the column's own
 * header getters return the aggregation cell. The column header must still be reachable.
 */
@SpringBootTest
class DataGridAggregationTopHeaderTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata
    @Autowired
    MessageTools messageTools

    @Override
    void setup() {
        registerViewBasePackages("component.grid", "io.jmix.flowui.app")
    }

    def "getDefaultHeaderText returns the column header, not the TOP aggregation cell"() {
        def view = openViewWithItems()
        def column = view.aggregationTopDataGrid.getColumnByKey("usages") as DataGridColumn<?>

        expect: "the column's own header text is the aggregation cell"
        column.headerText != "Usages header"

        and: "the default header text is the column header"
        column.defaultHeaderText == "Usages header"
    }

    def "getDefaultHeaderComponent returns the header filter of a filterable column under the TOP aggregation row"() {
        def view = openViewWithItems()
        def column = view.aggregationTopDataGrid.getColumnByKey("name") as DataGridColumn<?>

        expect:
        column.defaultHeaderComponent instanceof DataGridHeaderFilter
        ((column.defaultHeaderComponent as DataGridHeaderFilter).header as HasText).text == "Name header"
    }

    def "gridColumnVisibility menu items show the column headers under the TOP aggregation row"() {
        def view = openViewWithItems()
        def fromDateCaption = messageTools.getPropertyCaption(metadata.getClass(AggregationTestEntity), "fromDate")

        expect:
        view.columnVisibility.menuItems*.text == ["Name header", fromDateCaption, "Usages header"]
    }

    def "Header filter added after the TOP aggregation row keeps the column header"() {
        def view = openViewWithItems()
        def column = view.aggregationTopDataGrid.getColumnByKey("usages") as DataGridColumn<?>

        when:
        column.filterable = true

        then:
        def headerFilter = column.defaultHeaderComponent as DataGridHeaderFilter
        (headerFilter.header as HasText).text == "Usages header"
    }

    def "treeDataGrid column header and gridColumnVisibility menu under the TOP aggregation row"() {
        def view = openViewWithItems()
        def group = metadata.create(Group)
        group.name = "group1"
        view.groupsDc.items = [group]
        def column = view.aggregationTopTreeDataGrid.getColumnByKey("name") as DataGridColumn<?>

        expect: "the column's own header text is the aggregation cell"
        column.headerText != "Group name header"

        and:
        column.defaultHeaderText == "Group name header"
        view.treeColumnVisibility.menuItems*.text == ["Group name header"]
    }

    private DataGridAggregationTopHeaderTestView openViewWithItems() {
        def view = navigateToView(DataGridAggregationTopHeaderTestView)
        view.aggregationTestEntityDc.items = [
                new AggregationTestEntity(name: 'test1', usages: 25.5),
                new AggregationTestEntity(name: 'test2', usages: 50.5)
        ]
        return view
    }
}

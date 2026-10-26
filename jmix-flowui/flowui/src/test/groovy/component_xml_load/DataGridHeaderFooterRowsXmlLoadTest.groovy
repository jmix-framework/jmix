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

package component_xml_load

import component_xml_load.screen.BrokenGridRowsDisjointFooterGroupView
import component_xml_load.screen.BrokenGridRowsNoColumnsView
import component_xml_load.screen.BrokenGridRowsNotAdjacentView
import component_xml_load.screen.BrokenGridRowsNotNestedView
import component_xml_load.screen.BrokenGridRowsRepeatedColumnView
import component_xml_load.screen.BrokenGridRowsTextAndComponentView
import component_xml_load.screen.BrokenGridRowsTwoComponentsView
import component_xml_load.screen.BrokenGridRowsUnknownColumnView
import component_xml_load.screen.BrokenGridRowsWidenedSingleColumnView
import component_xml_load.screen.BrokenGridRowsWidenedSpanView
import component_xml_load.screen.DataGridHeaderFooterRowsView
import io.jmix.core.DataManager
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.grid.headerfilter.DataGridHeaderFilter
import io.jmix.flowui.exception.GuiDevelopmentException
import io.jmix.flowui.facet.settings.component.DataGridSettings
import io.jmix.flowui.facet.settings.component.binder.DataGridSettingsBinder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

import java.time.LocalDate

@SpringBootTest
class DataGridHeaderFooterRowsXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    DataGridSettingsBinder dataGridSettingsBinder

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen", "io.jmix.flowui.app")

        def order = dataManager.create(Order)
        order.number = "number"
        order.date = LocalDate.now()
        order.amount = BigDecimal.valueOf(5)

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        //noinspection SqlDialectInspection, SqlNoDataSourceInspection
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Declared header rows are built above the default header row"() {
        when: "Open a view with a two-level header"
        def grid = navigateToView(DataGridHeaderFooterRowsView).headerRowsDataGrid
        def rows = grid.headerRows
        def (number, date, amount, total) = ["number", "date", "amount", "total"].collect { grid.getColumnByKey(it) }

        then: "The declared rows go top to bottom above the default row"
        rows.size() == 3
        rows[2].is(grid.defaultHeaderRow)

        and: "The top row joins all columns, resolves a message key and gets a part name"
        def orderCell = rows[0].getCell(number)
        [date, amount, total].every { rows[0].getCell(it).is(orderCell) }
        orderCell.text == "Order"
        orderCell.partName == "order-group"

        and: "The second row joins two columns, fills a single-column cell and leaves an unlisted column empty"
        rows[1].getCell(number).is(rows[1].getCell(date))
        rows[1].getCell(number).text == "Identity"
        rows[1].getCell(amount).text == "Money"
        !rows[1].getCell(amount).is(rows[1].getCell(total))
        rows[1].getCell(total).text == ""

        and: "The default row keeps one cell per column"
        rows[2].cells.size() == 4
    }

    def "A hidden default header row is created when no column has a header"() {
        when: "Open a view whose grid has key-only columns and a header row"
        def grid = navigateToView(DataGridHeaderFooterRowsView).keyColumnsDataGrid
        def rows = grid.headerRows
        def (first, second) = ["first", "second"].collect { grid.getColumnByKey(it) }

        then: "The default row is created below the declared one and has no content"
        rows.size() == 2
        rows[1].is(grid.defaultHeaderRow)
        rows[1].cells.every { it.text == null && it.component == null }

        and: "The declared row joins the columns"
        rows[0].getCell(first).is(rows[0].getCell(second))
        rows[0].getCell(first).text == "Both"
    }

    def "Declared footer rows are built below a hidden column footer row"() {
        when: "Open a view with footer rows and no column footers"
        def grid = navigateToView(DataGridHeaderFooterRowsView).footerRowsDataGrid
        def rows = grid.footerRows
        def (number, date, amount, total) = ["number", "date", "amount", "total"].collect { grid.getColumnByKey(it) }

        then: "The column footer row comes first and has no content"
        rows.size() == 3
        rows[0].cells.every { it.text == null && it.component == null }

        and: "The first declared row joins two columns and fills a single-column cell"
        rows[1].getCell(number).is(rows[1].getCell(date))
        rows[1].getCell(number).text == "Identity footer"
        rows[1].getCell(amount).text == "Money footer"
        rows[1].getCell(total).text == ""

        and: "The second declared row joins all columns and gets a part name"
        def orderCell = rows[2].getCell(number)
        [date, amount, total].every { rows[2].getCell(it).is(orderCell) }
        orderCell.text == "Order footer"
        orderCell.partName == "order-footer"
    }

    def "Declared footer rows are built below the column footers"() {
        when: "Open a view with column footers and a footer row"
        def grid = navigateToView(DataGridHeaderFooterRowsView).columnFootersDataGrid
        def rows = grid.footerRows
        def (number, date) = ["number", "date"].collect { grid.getColumnByKey(it) }

        then: "The column footers stay in the first row"
        rows.size() == 2
        rows[0].getCell(number).text == "Number footer"
        rows[0].getCell(date).text == "Date footer"

        and: "The declared row follows them"
        rows[1].getCell(number).is(rows[1].getCell(date))
        rows[1].getCell(number).text == "Joined footer"
    }

    def "Footer cells join columns within a header group or across all header groups"() {
        when: "Open a view whose footer rows nest with the header groups"
        def grid = navigateToView(DataGridHeaderFooterRowsView).nestedGroupsDataGrid
        def rows = grid.footerRows
        def (number, date, amount, total) = ["number", "date", "amount", "total"].collect { grid.getColumnByKey(it) }

        then: "A footer cell inside one header group is joined"
        rows.size() == 3
        rows[1].getCell(amount).is(rows[1].getCell(total))
        !rows[1].getCell(amount).is(rows[1].getCell(date))
        rows[1].getCell(amount).text == "Money footer"

        and: "A footer cell covering all header groups is joined"
        [date, amount, total].every { rows[2].getCell(it).is(rows[2].getCell(number)) }
        rows[2].getCell(number).text == "Order footer"

        and: "The header groups are intact"
        grid.headerRows[0].getCell(number).is(grid.headerRows[0].getCell(date))
        grid.headerRows[0].getCell(amount).is(grid.headerRows[0].getCell(total))
    }

    def "Components in header and footer cells are placed, loaded and reachable by id"() {
        when: "Open a view with components in cells"
        def view = navigateToView(DataGridHeaderFooterRowsView)
        def grid = view.componentCellsDataGrid
        def (number, date, amount, total) = ["number", "date", "amount", "total"].collect { grid.getColumnByKey(it) }

        then: "The header component is placed into the joined cell and injected"
        def headerCell = grid.headerRows[0].getCell(number)
        headerCell.is(grid.headerRows[0].getCell(date))
        view.numberFilterField != null
        headerCell.component.is(view.numberFilterField)

        and: "The footer container is placed into the joined cell, its nested component is loaded and injected"
        def footerCell = grid.footerRows[1].getCell(number)
        footerCell.is(grid.footerRows[1].getCell(date))
        !footerCell.is(grid.footerRows[1].getCell(amount))
        footerCell.component.id.get() == "footerBox"
        view.footerButton.parent.get().is(footerCell.component)
        view.footerButton.text == "Footer button"
        UiComponentUtils.findComponent(view, "footerButton").get().is(view.footerButton)

        when: "Use the cell components"
        view.numberFilterField.setValue("filter")
        view.footerButton.click()

        then: "The controller subscriptions are invoked"
        view.numberFilterValue == "filter"
        view.footerButtonClicked
    }

    def "A filterable column inside a joined group keeps its header filter"() {
        when: "Open a view with filterable columns joined in a header row"
        def grid = navigateToView(DataGridHeaderFooterRowsView).filterableDataGrid
        def (number, date) = ["number", "date"].collect { grid.getColumnByKey(it) }

        then: "The filters stay in the default header row"
        number.filterable
        date.filterable
        grid.defaultHeaderRow.getCell(number).component instanceof DataGridHeaderFilter
        grid.defaultHeaderRow.getCell(date).component instanceof DataGridHeaderFilter

        and: "The declared row joins the columns above them"
        grid.headerRows[0].getCell(number).is(grid.headerRows[0].getCell(date))
        grid.headerRows[0].getCell(number).text == "Identity"
    }

    def "Aggregation rows keep their positions next to the declared rows"() {
        given: "A view with aggregated grids"
        def view = navigateToView(DataGridHeaderFooterRowsView)

        when: "Load the data"
        view.ordersDl.load()

        then: "The top aggregation row goes under the default header row"
        def top = view.topAggregationDataGrid
        def topRows = top.headerRows
        topRows.size() == 3
        topRows[0].getCell(top.getColumnByKey("number")).is(topRows[0].getCell(top.getColumnByKey("date")))
        topRows[0].getCell(top.getColumnByKey("number")).text == "Identity"
        topRows[1].is(top.defaultHeaderRow)
        !topRows[2].getCell(top.getColumnByKey("amount")).text.isEmpty()

        and: "The bottom aggregation row goes above the footer rows"
        def bottom = view.bottomAggregationDataGrid
        def bottomRows = bottom.footerRows
        bottomRows.size() == 3
        !bottomRows[0].getCell(bottom.getColumnByKey("amount")).text.isEmpty()
        bottomRows[2].getCell(bottom.getColumnByKey("number")).is(bottomRows[2].getCell(bottom.getColumnByKey("date")))
        bottomRows[2].getCell(bottom.getColumnByKey("number")).text == "Identity footer"
    }

    def "A treeDataGrid loads header and footer rows"() {
        when: "Open a view with a tree grid"
        def grid = navigateToView(DataGridHeaderFooterRowsView).headerRowsTreeDataGrid
        def (number, date) = ["number", "date"].collect { grid.getColumnByKey(it) }

        then: "The header row is declared above the default row"
        grid.headerRows.size() == 2
        grid.headerRows[0].getCell(number).is(grid.headerRows[0].getCell(date))
        grid.headerRows[0].getCell(number).text == "Tree identity"

        and: "The footer row is declared below the hidden column footer row"
        grid.footerRows.size() == 2
        grid.footerRows[1].getCell(number).is(grid.footerRows[1].getCell(date))
        grid.footerRows[1].getCell(number).text == "Tree footer"
    }

    def "Saved settings with a column order that splits a joined group keep the declared order"() {
        given: "A grid with joined header cells and settings that move a column out of its group"
        def grid = navigateToView(DataGridHeaderFooterRowsView).headerRowsDataGrid
        def settings = new DataGridSettings(id: "headerRowsDataGrid",
                columns: ["number", "amount", "date", "total"].collect { new DataGridSettings.Column(key: it) })

        when: "Apply the settings"
        dataGridSettingsBinder.applySettings(grid, settings)

        then: "The view keeps working with the declared column order"
        noExceptionThrown()
        grid.columns*.key == ["number", "date", "amount", "total"]
    }

    def "Throw an exception for a broken header or footer row declaration: #description"() {
        when: "Open a view with a broken row declaration"
        navigateToView(viewClass)

        then: "Throw GuiDevelopmentException"
        def e = thrown GuiDevelopmentException
        e.message.startsWith(message)

        where:
        description                           | viewClass                             | message
        "no columns attribute"                | BrokenGridRowsNoColumnsView           | "'headerRow' cell must define the 'columns' attribute"
        "unknown column"                      | BrokenGridRowsUnknownColumnView       | "'headerRow' cell 'number missing' refers to unknown column 'missing'"
        "column repeated in a row"            | BrokenGridRowsRepeatedColumnView      | "Column 'date' is listed in more than one cell of a 'headerRow'"
        "columns are not adjacent"            | BrokenGridRowsNotAdjacentView         | "'headerRow' cell 'number amount' cannot join its columns: Cannot join cells that are not adjacent"
        "span widened by another row"         | BrokenGridRowsWidenedSpanView         | "'headerRow' cell 'date amount' would also cover columns 'number', 'total' joined with its columns in another row"
        "single column in a joined group"     | BrokenGridRowsWidenedSingleColumnView | "'headerRow' cell 'date' would also cover columns 'number' joined with its columns in another row"
        "header and footer groups not nested" | BrokenGridRowsNotNestedView           | "'footerRow' cell 'number date' cannot join its columns: This set of cells can not be joined"
        "footer group beside a header group"  | BrokenGridRowsDisjointFooterGroupView | "'footerRow' cell 'amount total' cannot join its columns: This set of cells can not be joined"
        "text and a component"                | BrokenGridRowsTextAndComponentView    | "'headerRow' cell 'number date' cannot have both the 'text' attribute and a component"
        "two components"                      | BrokenGridRowsTwoComponentsView       | "'headerRow' cell 'number date' must contain at most one component element"
    }
}

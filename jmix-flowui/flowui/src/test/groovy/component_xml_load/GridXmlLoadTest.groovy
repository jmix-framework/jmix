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

import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.grid.ColumnTextAlign
import com.vaadin.flow.component.grid.Grid
import com.vaadin.flow.component.grid.dnd.GridDropMode
import com.vaadin.flow.component.html.Anchor
import com.vaadin.flow.component.html.AnchorTarget
import com.vaadin.flow.component.html.Hr
import com.vaadin.flow.component.icon.VaadinIcon
import component.standarddetailview.view.OrderDetailTestView
import component_xml_load.screen.GridView
import io.jmix.core.DataManager
import io.jmix.flowui.component.grid.DataGridColumn
import io.jmix.flowui.component.grid.EnhancedDataGrid
import io.jmix.flowui.component.grid.renderer.DetailButtonRenderer
import io.jmix.flowui.component.grid.renderer.DetailLinkRenderer
import io.jmix.flowui.kit.component.grid.GridMenuItemActionWrapper
import io.jmix.flowui.testassist.UiTestUtils
import io.jmix.flowui.view.OpenMode
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
        registerViewBasePackages("component_xml_load.screen", "component.standarddetailview.view", "io.jmix.flowui.app")

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

    def "Load dataGrid component from XML"() {
        given: "Screen with a dataGrid"
        def gridView = navigateToView(GridView.class)
        gridView.loadData()

        when: "dataGrid is loaded"
        def dataGrid = gridView.dataGrid

        then: "dataGrid attributes are loaded"
        verifyAll(dataGrid) {
            id.get() == "dataGrid"
            aggregatable
            aggregationPosition == EnhancedDataGrid.AggregationPosition.BOTTOM
            allRowsVisible
            ariaLabel.orElse(null) == "testAriaLabel"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
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
            element.getProperty("multiSortOnShiftClick", false)
            element.getAttribute("multi-sort-priority") == "append"
            nestedNullBehavior == Grid.NestedNullBehavior.THROW
            pageSize == 20
            rowsDraggable
            emptyStateText == "emptyStateText"
            tabIndex == 3
            themeNames.containsAll(["column-borders", "compact"])
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

    def "Load DataGrid with custom context menu"() {
        given: "Screen with a dataGrid"
        def gridView = navigateToView(GridView.class)
        gridView.loadData()

        when: "dataGridWithCustomContextMenu is loaded"
        def dataGridWithCustomContextMenu = gridView.dataGridWithCustomContextMenu

        then: "dataGrid contextMenu attributes are loaded"
        def contextMenu = dataGridWithCustomContextMenu.getContextMenu()
        verifyAll(contextMenu) {
            id.get() == "contextMenu"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            !visible
        }
        def contextMenuItems = contextMenu.getItems()
        contextMenuItems.size() == 2
        def menu1Item = contextMenuItems.get(0)

        def menuItemComponent = menu1Item.getChildren().findFirst().orElse(null)
        verifyAll(menu1Item) {
            id.get() == "menu1"
            classNames.containsAll(["cssClassName3", "cssClassName4"])
            style.get("color") == "blue"
            enabled
            visible
            menuItemComponent != null
            menuItemComponent instanceof GridMenuItemActionWrapper<?>
            (menuItemComponent as GridMenuItemActionWrapper<?>).prefixComponent.element.getAttribute("icon") ==
                    VaadinIcon.ABACUS.create().element.getAttribute("icon")
            (menuItemComponent as GridMenuItemActionWrapper<?>).text == "Menu 1"
            (menuItemComponent as GridMenuItemActionWrapper<?>).whiteSpace == HasText.WhiteSpace.NOWRAP
        }

        def menu1Items = menu1Item.getSubMenu().getItems()
        menu1Items.size() == 2
        def item1 = menu1Items.get(0)
        def item1Component = item1.getChildren().findFirst().orElse(null)
        verifyAll(item1) {
            id.get() == "item1"
            item1Component != null
            item1Component instanceof GridMenuItemActionWrapper<?>
            (item1Component as GridMenuItemActionWrapper<?>).text == "Item 1"
        }

        def item2 = menu1Items.get(1)
        def item2Component = item2.getChildren().findFirst().orElse(null)
        verifyAll(item2) {
            id.get() == "item2"
            item2Component != null
            item2Component instanceof GridMenuItemActionWrapper<?>
            (item2Component as GridMenuItemActionWrapper<?>).text == "Item 2"
        }

        //separator
        menu1Item.getSubMenu().getChildren().toList().get(1) instanceof Hr

        def item3 = contextMenuItems.get(1)

        def item3Component = item3.getChildren().findFirst().orElse(null)
        verifyAll(item3) {
            id.get() == "item3"
            item3Component != null
            item3Component instanceof GridMenuItemActionWrapper<?>
            (item3Component as GridMenuItemActionWrapper<?>).text == "Item 3"
        }

        //separator
        contextMenu.getChildren().toList().get(1) instanceof Hr
    }

    def "Load DataGrid with custom empty state component"() {
        given: "View with a dataGrid"
        def gridView = navigateToView(GridView.class)

        when: "dataGrid is loaded"
        def dataGrid = gridView.dataGridWithCustomEmptyStateComponent

        then: "emptyStateComponent is loaded"
        gridView.emptyStateButton != null
        gridView.emptyStateButton == dataGrid.emptyStateComponent
        !dataGrid.items.items

        when: "emptyStateComponent is clicked"
        gridView.emptyStateButton.click()

        then: "dataGrid items will be loaded"
        dataGrid.items.items
    }

    def "Load dataGrid detail renderers from XML"() {
        given: "Screen with a dataGrid"
        def gridView = navigateToView(GridView.class)
        gridView.loadData()

        when: "detail renderers dataGrid is loaded"
        def order = gridView.detailRenderersDataGrid.items.items[0] as Order
        def linkRenderer = gridView.detailRenderersDataGrid.getColumnByKey("number").renderer as DetailLinkRenderer
        def buttonRenderer = gridView.detailRenderersDataGrid.getColumnByKey("date").renderer as DetailButtonRenderer

        then: "detailLinkRenderer attributes are loaded"
        verifyAll(linkRenderer) {
            target == AnchorTarget.BLANK
            classNames == "detail-link, test-link"
            css == "color: blue; font-weight: 600;"
        }

        when: "detail link component is created"
        Anchor anchor = linkRenderer.createComponent(order) as Anchor

        then: "detail link component is configured"
        verifyAll(anchor) {
            text == "Link"
            href.contains("OrderDetailCustomRouteParamTestView")
            href.contains(order.id.toString())
            target.get() == AnchorTarget.BLANK.value
            classNames.containsAll(["detail-link", "test-link"])
            style.get("color") == "blue"
            style.get("font-weight") == "600"
        }

        and: "detailButtonRenderer attributes are loaded"
        verifyAll(buttonRenderer) {
            openMode == OpenMode.NAVIGATION
            iconProvider != null
            themeNames == "tertiary-inline, small"
            classNames == "detail-button, test-button"
            css == "color: green;"
        }

        when: "detail button component is created"
        def button = buttonRenderer.createComponent(order)

        then: "detail button component is configured"
        verifyAll(button) {
            text == "Open order"
            icon != null
            themeNames.containsAll(["tertiary-inline", "small"])
            classNames.containsAll(["detail-button", "test-button"])
            style.get("color") == "green"
        }

        when: "detail button is clicked"
        button.click()

        then: "detail view is opened"
        UiTestUtils.getCurrentView() instanceof OrderDetailTestView
    }

    def "Load treeDataGrid component from XML"() {
        given: "Screen with a treeDataGrid"
        def gridView = navigateToView(GridView.class)
        gridView.loadData()

        when: "treeDataGrid is loaded"
        def treeDataGrid = gridView.treeDataGrid

        then: "treeDataGrid attributes are loaded"
        verifyAll(treeDataGrid) {
            id.get() == "treeDataGrid"
            aggregatable
            aggregationPosition == EnhancedDataGrid.AggregationPosition.BOTTOM
            allRowsVisible
            ariaLabel.orElse(null) == "testAriaLabel"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
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
            element.getProperty("multiSortOnShiftClick", false)
            element.getAttribute("multi-sort-priority") == "append"
            nestedNullBehavior == Grid.NestedNullBehavior.THROW
            pageSize == 20
            rowsDraggable
            emptyStateText == "emptyStateText"
            tabIndex == 3
            themeNames.containsAll(["column-borders", "compact"])
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

    def "Load TreeDataGrid with custom empty state component"() {
        given: "View with a treeDataGrid"
        def gridView = navigateToView(GridView.class)

        when: "treeDataGrid is loaded"
        def treeDataGrid = gridView.treeDataGridWithCustomEmptyStateComponent

        then: "emptyStateComponent is loaded"
        gridView.treeDataGridEmptyStateButton != null
        gridView.treeDataGridEmptyStateButton == treeDataGrid.emptyStateComponent.getChildren().findAny().orElse(null)
        !treeDataGrid.items.items

        when: "treeDataGridEmptyStateComponent is clicked"
        gridView.treeDataGridEmptyStateButton.click()

        then: "treeDataGrid items will be loaded"
        treeDataGrid.items.items
    }

    def "Load TreeDataGrid with custom context menu"() {
        given: "Screen with a treeDataGrid"
        def gridView = navigateToView(GridView.class)
        gridView.loadData()

        when: "treeDataGridWithCustomContextMenu is loaded"
        def treeDataGridWithCustomContextMenu = gridView.treeDataGridWithCustomContextMenu

        then: "dataGrid contextMenu attributes are loaded"
        def contextMenu = treeDataGridWithCustomContextMenu.getContextMenu()
        verifyAll(contextMenu) {
            id.get() == "contextMenu"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            !visible
        }
        def contextMenuItems = contextMenu.getItems()
        contextMenuItems.size() == 2
        def menu1Item = contextMenuItems.get(0)

        def menuItemComponent = menu1Item.getChildren().findFirst().orElse(null)
        verifyAll(menu1Item) {
            id.get() == "menu1"
            classNames.containsAll(["cssClassName3", "cssClassName4"])
            style.get("color") == "blue"
            enabled
            visible
            menuItemComponent != null
            menuItemComponent instanceof GridMenuItemActionWrapper<?>
            (menuItemComponent as GridMenuItemActionWrapper<?>).prefixComponent.element.getAttribute("icon") ==
                    VaadinIcon.ABACUS.create().element.getAttribute("icon")
            (menuItemComponent as GridMenuItemActionWrapper<?>).text == "Menu 1"
            (menuItemComponent as GridMenuItemActionWrapper<?>).whiteSpace == HasText.WhiteSpace.NOWRAP
        }

        def menu1Items = menu1Item.getSubMenu().getItems()
        menu1Items.size() == 2
        def item1 = menu1Items.get(0)
        def item1Component = item1.getChildren().findFirst().orElse(null)
        verifyAll(item1) {
            id.get() == "item1"
            item1Component != null
            item1Component instanceof GridMenuItemActionWrapper<?>
            (item1Component as GridMenuItemActionWrapper<?>).text == "Item 1"
        }

        def item2 = menu1Items.get(1)
        def item2Component = item2.getChildren().findFirst().orElse(null)
        verifyAll(item2) {
            id.get() == "item2"
            item2Component != null
            item2Component instanceof GridMenuItemActionWrapper<?>
            (item2Component as GridMenuItemActionWrapper<?>).text == "Item 2"
        }

        //separator
        menu1Item.getSubMenu().getChildren().toList().get(1) instanceof Hr

        def item3 = contextMenuItems.get(1)

        def item3Component = item3.getChildren().findFirst().orElse(null)
        verifyAll(item3) {
            id.get() == "item3"
            item3Component != null
            item3Component instanceof GridMenuItemActionWrapper<?>
            (item3Component as GridMenuItemActionWrapper<?>).text == "Item 3"
        }

        //separator
        contextMenu.getChildren().toList().get(1) instanceof Hr
    }

    def "Load dataGrid columns' filterable, sortable and resizable attributes from XML"() {
        given: "Screen with a dataGrid"
        def gridView = navigateToView(GridView.class)

        when: "dataGrid is loaded"
        def columnsAttributesDataGrid = gridView.columnsAttributesDataGrid

        then: "dataGrid columns' attributes are loaded"
        columnsAttributesDataGrid.headerFilterApplyShortcut == "CONTROL-ENTER"
        verifyAll(columnsAttributesDataGrid.getColumnByKey("number") as DataGridColumn) {
            sortable
            resizable
            filterable
        }

        verifyAll(columnsAttributesDataGrid.getColumnByKey("amount") as DataGridColumn) {
            !sortable
            !resizable
            filterable
        }

        verifyAll(columnsAttributesDataGrid.getColumnByKey("date") as DataGridColumn) {
            !sortable
            resizable
            !filterable
        }
    }
}

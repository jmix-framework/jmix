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

package data_components

import com.vaadin.flow.component.grid.GridSortOrder
import io.jmix.core.Metadata
import io.jmix.core.Sort
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.data.grid.ContainerDataGridItems
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.CollectionPropertyContainer
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.InstanceContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(classes = CollectionContainerSortProviderTestConfiguration)
class CollectionContainerSortProviderTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    Metadata metadata
    @Autowired
    UiComponents uiComponents

    private CollectionContainer<Order> container

    void setup() {
        container = dataComponents.createCollectionContainer(Order)
    }

    def "test providers are applied in order and provided sorter supports multiple properties"() {
        def first = order("a", 20d)
        def second = order("b", 10d)
        def third = order("c", 30d)

        when:
        container.items = [first, second, third]
        container.sorter.sort(Sort.by(Sort.Order.asc("number")))

        then: "The first provider wins over another provider for the same container"
        container.items == [second, first, third]

        when:
        container.items = [third, first, second]
        container.sorter.sort(Sort.by(Sort.Order.asc("total")))

        then:
        container.items == [first, second, third]
    }

    def "test descending sort reverses provided sorter comparator"() {
        def first = order("a", 20d)
        def second = order("b", 10d)
        def third = order("c", 30d)

        when:
        container.items = [first, second, third]
        container.sorter.sort(Sort.by(Sort.Order.desc("number")))

        then:
        container.items == [third, first, second]
    }

    def "test explicit grid comparator overrides provided sorter comparator"() {
        def first = order("a", 20d)
        def second = order("b", 10d)
        def third = order("c", 30d)

        DataGrid<Order> dataGrid = uiComponents.create(DataGrid)
        def numberColumn = dataGrid.addColumn(metadata.getClass(Order).getPropertyPath("number"))
        numberColumn.setComparator({ Order left, Order right -> left.number <=> right.number } as Comparator<Order>)
        dataGrid.setItems(new ContainerDataGridItems(container))

        when:
        container.items = [first, second, third]
        dataGrid.sort(GridSortOrder.desc(numberColumn).build())

        then:
        container.items == [third, second, first]
    }

    def "test provider can distinguish collection property container"() {
        InstanceContainer<Order> orderCt = dataComponents.createInstanceContainer(Order)
        CollectionPropertyContainer<OrderLine> linesCt =
                dataComponents.createCollectionContainer(OrderLine, orderCt, "orderLines")

        def order = metadata.create(Order)
        def first = orderLine(order, 1, "c")
        def second = orderLine(order, 2, "a")
        def third = orderLine(order, 3, "b")
        order.orderLines = [first, second, third]
        orderCt.item = order

        when:
        linesCt.sorter.sort(Sort.by(Sort.Order.asc("quantity")))

        then:
        linesCt.items == [second, third, first]
    }

    private Order order(String number, Double total) {
        Order order = metadata.create(Order)
        order.number = number
        order.total = total
        return order
    }

    private OrderLine orderLine(Order order, Integer quantity, String description) {
        OrderLine line = metadata.create(OrderLine)
        line.order = order
        line.quantity = quantity
        line.description = description
        return line
    }
}

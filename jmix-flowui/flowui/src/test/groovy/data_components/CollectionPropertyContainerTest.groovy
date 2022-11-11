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

package data_components

import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.CollectionPropertyContainer
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.InstanceContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

import java.util.function.Consumer

@SpringBootTest
class CollectionPropertyContainerTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata

    private Order order
    private OrderLine orderLine1
    private OrderLine orderLine2

    private InstanceContainer<Order> orderCt
    private CollectionPropertyContainer<OrderLine> linesCt

    @Override
    void setup() {
        orderCt = dataComponents.createInstanceContainer(Order)
        linesCt = dataComponents.createCollectionContainer(OrderLine, orderCt, 'orderLines')

        order = metadata.create(Order)
        order.number = '111'

        orderLine1 = metadata.create(OrderLine)
        orderLine1.order = order
        orderLine1.quantity = 1
        orderLine2 = metadata.create(OrderLine)
        orderLine2.order = order
        orderLine2.quantity = 2
        order.orderLines = [orderLine1, orderLine2]
    }

    def "property reflects changes when using getMutableItems"() {

        orderCt.setItem(order)

        when:

        linesCt.getMutableItems().remove(orderLine2)

        then:

        order.orderLines.contains(orderLine1)
        !order.orderLines.contains(orderLine2)
    }

    def "property reflects changes when using replaceItem"() {

        orderCt.setItem(order)
        OrderLine line1 = metadata.create(OrderLine)
        line1.id = orderLine1.id
        line1.order = order
        line1.quantity = 11

        when:
        linesCt.replaceItem(line1)

        then:
        def replacedLine = order.orderLines.find { it.id == orderLine1.id }
        replacedLine.is(line1)
        replacedLine.quantity == 11
    }

    def "property does not reflect changes when using getDisconnectedItems"() {

        orderCt.setItem(order)

        when:

        linesCt.getDisconnectedItems().remove(orderLine2)

        then:

        order.orderLines.containsAll([orderLine1, orderLine2])
    }

    def "property reflects changes if new collection is set using setItems"() {

        orderCt.setItem(order)

        OrderLine orderLine3 = metadata.create(OrderLine)
        orderLine3.order = order
        orderLine3.quantity = 3

        OrderLine orderLine4 = metadata.create(OrderLine)
        orderLine4.order = order
        orderLine4.quantity = 4
        def otherLines = [orderLine3, orderLine4]

        when:

        linesCt.setItems(otherLines)

        then:

        !order.orderLines.contains(orderLine1)
        !order.orderLines.contains(orderLine2)
        order.orderLines.containsAll(orderLine3, orderLine4)
    }

    def "property does not reflect changes if new collection is set using setDisconnectedItems"() {

        orderCt.setItem(order)

        OrderLine orderLine3 = metadata.create(OrderLine)
        orderLine3.order = order
        orderLine3.quantity = 3

        OrderLine orderLine4 = metadata.create(OrderLine)
        orderLine4.order = order
        orderLine4.quantity = 4
        def otherLines = [orderLine3, orderLine4]

        when:

        linesCt.setDisconnectedItems(otherLines)

        then:

        order.orderLines.containsAll(orderLine1, orderLine2)
        !order.orderLines.contains(orderLine3)
        !order.orderLines.contains(orderLine4)
    }

    def "master container is notified when the collection is changed"() {

        orderCt.setItem(order)

        OrderLine orderLine3 = metadata.create(OrderLine)
        orderLine3.order = order
        orderLine3.quantity = 3

        Consumer<InstanceContainer.ItemPropertyChangeEvent<Order>> listener = Mock()
        orderCt.addItemPropertyChangeListener(listener)

        when:

        linesCt.getMutableItems().add(orderLine3)

        then:

        1 * listener.accept(_)
    }

    def "sorting using getMutableItems"() {

        orderCt.setItem(order)

        Comparator<OrderLine> comparator = Comparator.comparing { OrderLine line -> line.getQuantity() }.reversed()

        when:

        linesCt.getMutableItems().sort(comparator)

        then:

        order.orderLines == [orderLine2, orderLine1]
    }

    def "items added to mutableItems produce PropertyChangeEvent"() {
        orderCt.setItem(order)
        OrderLine orderLine3 = metadata.create(OrderLine)
        orderLine3.order = order
        orderLine3.quantity = 3

        def listener = Mock(Consumer)

        linesCt.mutableItems.add(orderLine3)
        linesCt.addItemPropertyChangeListener(listener)

        when:
        orderLine3.quantity = 33

        then:
        1 * listener.accept(_) >> { List arguments ->
            InstanceContainer.ItemPropertyChangeEvent event = arguments[0]
            assert event.item == orderLine3
            assert event.property == 'quantity'
            assert event.value == 33
        }
    }

    def "items added to nested collection does NOT produce CollectionChangeEvent"() {
        orderCt.setItem(order)
        OrderLine orderLine3 = metadata.create(OrderLine)
        orderLine3.order = order
        orderLine3.quantity = 3

        def listener = Mock(Consumer)

        def subscription = linesCt.addCollectionChangeListener(listener)

        when:
        order.orderLines.add(orderLine3)

        then:
        0 * listener.accept(_) >> { List arguments ->
            CollectionContainer.CollectionChangeEvent event = arguments[0]
            assert event.changes.contains(orderLine3)
        }

        cleanup:
        subscription.remove()
    }
}

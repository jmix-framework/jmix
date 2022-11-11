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

import com.vaadin.flow.component.grid.GridSortOrder
import io.jmix.core.EntityStates
import io.jmix.core.Metadata
import io.jmix.core.Sort
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.grid.DataGrid
import io.jmix.flowui.data.grid.ContainerDataGridItems
import io.jmix.flowui.model.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

import java.util.function.Consumer

@SpringBootTest
class CollectionContainerUsageFlowuiTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata
    @Autowired
    EntityStates entityStates

    private CollectionContainer<Order> container
    private DataGrid<Order> dataGrid

    void setup() {
        container = dataComponents.createCollectionContainer(Order)
        dataGrid = uiComponents.create(DataGrid)
        dataGrid.addColumn(metadata.getClass(Order).getPropertyPath('number'))
        dataGrid.setItems(new ContainerDataGridItems(this.container))
    }

    def "sort items"() {

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'
        Order order3 = metadata.create(Order)
        order3.number = 'order3'

        when: "assigning a collection and selecting the first item"

        container.items = [order1, order2, order3]
        dataGrid.select(order1)

        then: "container's current item is the first"

        container.items.indexOf(container.item) == 0

        when: "assigning a collection with reverse order"

        container.setItems(container.items.reverse())

        then: "container's current item is the last"

        container.items.indexOf(container.item) == 2
    }

    def "sort items in-place"() {

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'
        Order order3 = metadata.create(Order)
        order3.number = 'order3'

        when: "assigning a collection and selecting the first item"

        container.items = [order1, order2, order3]
        dataGrid.select(order1)

        then: "container's current item is the first"

        container.items.indexOf(container.item) == 0

        when: "sorting the collection in reverse order in-place"

        container.getMutableItems().sort { it.number }

        then: "container's current item is the first again"

        container.items.indexOf(container.item) == 0
    }

    def "sort items using Sorter"() {

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'
        Order order3 = metadata.create(Order)
        order3.number = 'order3'

        container.items = [order2, order1, order3]

        when:

        container.getSorter().sort(Sort.by(Sort.Order.asc('number')))

        then:

        container.items == [order1, order2, order3]

        when:

        dataGrid.sort(GridSortOrder.desc(dataGrid.getColumnByKey('number')).build()) //'number', Table.SortDirection.DESCENDING

        then:

        container.items == [order3, order2, order1]
    }

    def "filter items"() {

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'
        Order order3 = metadata.create(Order)
        order3.number = 'order3'

        when: "assigning a collection and selecting the first item"

        container.items = [order1, order2, order3]
        dataGrid.select(order1)

        then: "container's current item is the first"

        container.items.indexOf(container.item) == 0

        when: "assigning a collection with absent item"

        container.items = [order2, order3]

        then: "container has no current item"

        container.itemOrNull == null
    }

    def "filter items in-place"() {

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'
        Order order3 = metadata.create(Order)
        order3.number = 'order3'

        when: "assigning a collection and selecting the first item"

        container.items = [order1, order2, order3]
        dataGrid.select(order1)

        then: "container's current item is the first"

        container.items.indexOf(container.item) == 0

        when: "removing the first item"

        container.mutableItems.remove(order1)

        then: "container has no current item"

        container.itemOrNull == null
    }

    def "table doesn't track container's item"() {

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'

        container.items = [order1, order2]
        dataGrid.select(order1)

        when:

        container.item == order2

        then:

        dataGrid.getSingleSelectedItem() == order1
    }

    def "add item in memory only"() {

        def modified = []
        DataContext context = dataComponents.createDataContext()
        context.addPreSaveListener({ e ->
            modified.addAll(e.modifiedInstances)
        })

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        context.merge(order1)
        container.items = [order1]

        Order order2 = metadata.create(Order)
        order2.number = 'order2'

        when:

        container.mutableItems.add(order2)
        dataGrid.select(order2)

        then:

        container.item == order2

        when:

        context.save()

        then:

        modified.size() == 1
        modified.contains(order1)
    }

    def "add item and save it"() {

        def modified = []
        DataContext context = dataComponents.createDataContext()
        context.addPreSaveListener({ e ->
            modified.addAll(e.modifiedInstances)
        })

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        context.merge(order1)
        container.items = [order1]

        Order order2 = metadata.create(Order)
        order2.number = 'order2'

        when:

        container.mutableItems.add(order2)
        context.merge(order2)
        dataGrid.select(order2)

        then:

        container.item == order2

        when:

        context.save()

        then:

        modified.size() == 2
        modified.containsAll(order1, order2)
    }

    def "remove item and save it"() {

        def modified = []
        DataContext context = dataComponents.createDataContext()
        context.addPreSaveListener({ e ->
            modified.addAll(e.removedInstances)
        })

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        entityStates.makeDetached(order1)
        context.merge(order1)
        container.items = [order1]

        when:

        container.mutableItems.remove(order1)
        context.remove(order1)

        then:

        container.itemOrNull == null

        when:

        context.save()

        then:

        modified.size() == 1
        modified.contains(order1)
    }

    def "remove item in memory only"() {

        def modified = []
        DataContext context = dataComponents.createDataContext()
        context.addPreSaveListener({ e ->
            modified.addAll(e.removedInstances)
        })

        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        context.merge(order1)
        container.items = [order1]

        when:

        container.mutableItems.remove(order1)

        then:

        container.itemOrNull == null

        when:

        context.save()

        then:

        modified.size() == 0
    }

    def "remove single item with fired event"() {
        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'
        Order order3 = metadata.create(Order)
        order3.number = 'order3'

        def listener = Mock(Consumer)

        container.items = [order1, order2, order3]
        container.addCollectionChangeListener(listener)

        when: "removing the first item"

        container.mutableItems.remove(order1)

        then: "container fired event with item"

        1 * listener.accept(_) >> { List arguments ->
            CollectionContainer.CollectionChangeEvent event = arguments[0]
            assert event.changeType == CollectionChangeType.REMOVE_ITEMS
            assert event.changes == [order1]
        }
    }

    def "remove multiple items with fired event"() {
        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'
        Order order3 = metadata.create(Order)
        order3.number = 'order3'

        def listener = Mock(Consumer)

        container.items = [order1, order2, order3]
        container.addCollectionChangeListener(listener)

        when: "remove 2 items"

        container.mutableItems.removeAll([order2, order3])

        then: "container fired event with items"

        1 * listener.accept(_) >> { List arguments ->
            CollectionContainer.CollectionChangeEvent event = arguments[0]
            assert event.changeType == CollectionChangeType.REMOVE_ITEMS
            assert event.changes == [order2, order3]
        }
    }

    def "items added to mutableItems produce PropertyChangeEvent"() {
        Order order1 = metadata.create(Order)
        order1.number = 'order1'

        def listener = Mock(Consumer)

        container.mutableItems.add(order1)
        container.addItemPropertyChangeListener(listener)

        when:
        order1.number = '111'

        then:
        1 * listener.accept(_) >> { List arguments ->
            InstanceContainer.ItemPropertyChangeEvent event = arguments[0]
            assert event.item == order1
            assert event.property == 'number'
            assert event.value == '111'
        }
    }

    def "PropertyChangedEvent is not lost after selecting another item"() {
        Order order1 = metadata.create(Order)
        order1.number = 'order1'
        Order order2 = metadata.create(Order)
        order2.number = 'order2'

        def listener = Mock(Consumer)

        container.items = [order1, order2]
        container.addItemPropertyChangeListener(listener)

        when:

        container.setItem(order1)
        container.setItem(order2)
        order1.number = 'order11'

        then:

        1 * listener.accept(_) >> { List arguments ->
            InstanceContainer.ItemPropertyChangeEvent event = arguments[0]
            assert event.item == order1
            assert event.property == 'number'
            assert event.value == 'order11'
        }
    }
}

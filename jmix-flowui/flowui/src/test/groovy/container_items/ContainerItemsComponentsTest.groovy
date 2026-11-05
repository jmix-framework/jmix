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

package container_items

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.data.provider.Query
import container_items.view.ContainerItemsTestView
import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.SupportsMetaClass
import io.jmix.flowui.component.checkboxgroup.JmixCheckboxGroup
import io.jmix.flowui.component.combobox.EntityComboBox
import io.jmix.flowui.component.gridlayout.GridLayout
import io.jmix.flowui.component.listbox.JmixListBox
import io.jmix.flowui.component.listbox.JmixMultiSelectListBox
import io.jmix.flowui.component.multiselectcombobox.JmixMultiSelectComboBox
import io.jmix.flowui.component.multiselectcomboboxpicker.JmixMultiSelectComboBoxPicker
import io.jmix.flowui.component.radiobuttongroup.JmixRadioButtonGroup
import io.jmix.flowui.component.select.JmixSelect
import io.jmix.flowui.component.twincolumn.TwinColumn
import io.jmix.flowui.component.virtuallist.JmixVirtualList
import io.jmix.flowui.data.SupportsDataProvider
import io.jmix.flowui.data.SupportsItemsContainer
import io.jmix.flowui.data.SupportsValueSource
import io.jmix.flowui.data.value.ContainerValueSource
import io.jmix.flowui.model.CollectionContainer
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.InstanceContainer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicInteger

/**
 * Components whose items come from a {@link CollectionContainer}: the container must not keep a component
 * in memory after it is dropped, and a component that is still referenced must keep getting container changes.
 */
@SpringBootTest
class ContainerItemsComponentsTest extends FlowuiTestSpecification {

    static final List<Class<? extends Component>> COMPONENT_TYPES = [
            EntityComboBox, JmixCheckboxGroup, GridLayout, JmixListBox, JmixMultiSelectListBox,
            JmixMultiSelectComboBox, JmixMultiSelectComboBoxPicker, JmixRadioButtonGroup, JmixSelect,
            TwinColumn, JmixVirtualList
    ]

    static final List<Class<? extends Component>> SINGLE_VALUE_TYPES = [
            EntityComboBox, JmixListBox, JmixRadioButtonGroup, JmixSelect
    ]

    static final List<Class<? extends Component>> BOUND_SINGLE_VALUE_TYPES = [
            EntityComboBox, JmixRadioButtonGroup, JmixSelect
    ]

    @Autowired
    UiComponents uiComponents

    @Autowired
    DataComponents dataComponents

    @Autowired
    Metadata metadata

    CollectionContainer<Customer> customersDc
    Customer customer1
    Customer customer2
    Customer customer3

    @Override
    void setup() {
        registerViewBasePackages("container_items.view")

        customer1 = createCustomer("Customer 1")
        customer2 = createCustomer("Customer 2")
        customer3 = createCustomer("Customer 3")

        customersDc = dataComponents.createCollectionContainer(Customer)
        customersDc.setItems([customer1, customer2])
    }

    def "#type.simpleName removed from the view is not kept in memory by the items container"() {
        given: "A view"
        def view = navigateToView(ContainerItemsTestView)

        when: "A component with items from the container is added to the view, removed and dropped"
        def componentRef = addRemoveAndDrop(view, type)

        then: "The component can be garbage-collected while the container is still in use"
        isGarbageCollected(componentRef)
        customersDc.items.size() == 2

        where:
        type << COMPONENT_TYPES
    }

    def "#type.simpleName attached to the view gets container changes after a garbage collection"() {
        given: "A component with items from the container attached to the view"
        def view = navigateToView(ContainerItemsTestView)
        def component = createWithItems(type)
        view.content.add(component)
        def dataChanges = countDataChanges(component)

        when: "Garbage is collected and the container items change"
        collectGarbage()
        customersDc.setItems([customer1, customer2, customer3])

        then: "The component's data provider reports the change"
        dataChanges.get() > 0
        dataViewItemCount(component) == 3

        where:
        type << COMPONENT_TYPES
    }

    def "#type.simpleName attached to the view gets item property changes after a garbage collection"() {
        given: "A component with items from the container attached to the view"
        def view = navigateToView(ContainerItemsTestView)
        def component = createWithItems(type)
        view.content.add(component)
        def dataChanges = countDataChanges(component)

        when: "Garbage is collected and a property of a container item changes"
        collectGarbage()
        customer1.name = "Customer 1 renamed"

        then: "The component's data provider reports the change"
        dataChanges.get() > 0

        where:
        type << COMPONENT_TYPES
    }

    def "#type.simpleName removed from the view gets container changes and shows them when added again"() {
        given: "A component with items from the container added to the view and removed"
        def view = navigateToView(ContainerItemsTestView)
        def component = createWithItems(type)
        view.content.add(component)
        view.content.remove(component)
        def dataChanges = countDataChanges(component)

        when: "Garbage is collected and the container items change while the component is removed"
        collectGarbage()
        customersDc.setItems([customer1, customer2, customer3])

        then: "The component's data provider reports the change"
        dataChanges.get() > 0

        when: "The component is added again"
        view.content.add(component)

        then: "It shows the current items"
        dataViewItemCount(component) == 3

        where:
        type << COMPONENT_TYPES
    }

    def "#type.simpleName never attached gets container changes after a garbage collection"() {
        given: "A component with items from the container that is not attached to any view"
        navigateToView(ContainerItemsTestView)
        def component = createWithItems(type)
        def dataChanges = countDataChanges(component)

        when: "Garbage is collected and the container items change"
        collectGarbage()
        customersDc.setItems([customer1, customer2, customer3])

        then: "The component's data provider reports the change"
        dataChanges.get() > 0
        dataViewItemCount(component) == 3

        where:
        type << COMPONENT_TYPES
    }

    def "#type.simpleName keeps its value when the container items are reloaded"() {
        given: "A component with items from the container attached to the view and a value selected"
        def view = navigateToView(ContainerItemsTestView)
        def component = createWithItems(type)
        view.content.add(component)
        (component as HasValue).setValue(value(customer1))

        when: "Garbage is collected and the container is reloaded with the selected item and a new one"
        collectGarbage()
        customersDc.setItems([customer1, customer2, customer3])

        then: "The value is kept"
        (component as HasValue).getValue() == value(customer1)

        where:
        type                          | value
        EntityComboBox                | { it }
        JmixListBox                   | { it }
        JmixRadioButtonGroup          | { it }
        JmixSelect                    | { it }
        JmixCheckboxGroup             | { [it] as Set }
        JmixMultiSelectListBox        | { [it] as Set }
        JmixMultiSelectComboBox       | { [it] as Set }
        JmixMultiSelectComboBoxPicker | { [it] as Set }
    }

    def "#type.simpleName bound to an entity attribute does not change the entity when the container items are reloaded"() {
        given: "An order merged into a DataContext with a customer selected"
        def view = navigateToView(ContainerItemsTestView)
        def dataContext = dataComponents.createDataContext()
        InstanceContainer<Order> orderDc = dataComponents.createInstanceContainer(Order)
        def order = metadata.create(Order)
        order.customer = customer1
        orderDc.setItem(dataContext.merge(order))

        and: "A component with items from the container bound to the order's customer"
        def component = createWithItems(type)
        view.content.add(component)
        (component as SupportsValueSource).setValueSource(new ContainerValueSource(orderDc, "customer"))

        and: "A listener counting DataContext changes"
        def contextChanges = new AtomicInteger()
        dataContext.addChangeListener { contextChanges.incrementAndGet() }

        when: "Garbage is collected and the container is reloaded"
        collectGarbage()
        customersDc.setItems([customer1, customer2, customer3])

        then: "The value is kept and the DataContext sees no change"
        (component as HasValue).getValue() == customer1
        contextChanges.get() == 0

        where:
        type << BOUND_SINGLE_VALUE_TYPES
    }

    def "#type.simpleName given items from another container no longer selects items of the previous one"() {
        given: "Two containers with the same customers"
        def view = navigateToView(ContainerItemsTestView)
        CollectionContainer<Customer> otherCustomersDc = dataComponents.createCollectionContainer(Customer)
        otherCustomersDc.setItems([customer1, customer2])

        and: "A component that took its items from the first container and then from the second one"
        def component = createWithItems(type)
        (component as SupportsItemsContainer).setItems(otherCustomersDc)
        view.content.add(component)

        when: "A value is selected"
        (component as HasValue).setValue(customer2)

        then: "Only the current container follows the selection"
        otherCustomersDc.item == customer2
        customersDc.itemOrNull == null

        where:
        type << SINGLE_VALUE_TYPES
    }

    def "Container drops its listeners of components that were garbage-collected"() {
        given: "A view and the number of listeners on the container"
        def view = navigateToView(ContainerItemsTestView)
        def listenersBefore = containerListenerCount(customersDc)

        when: "Several components with items from the container are added, removed, dropped and collected"
        def componentRefs = (1..5).collect { addRemoveAndDrop(view, EntityComboBox) }
        def allCollected = componentRefs.every { isGarbageCollected(it) }

        and: "The container fires the next event"
        customersDc.setItems([customer1, customer2, customer3])

        then: "The listeners of the collected components are gone"
        allCollected
        containerListenerCount(customersDc) == listenersBefore
    }

    protected Customer createCustomer(String name) {
        def customer = metadata.create(Customer)
        customer.name = name
        return customer
    }

    protected Component createWithItems(Class<? extends Component> type) {
        def component = uiComponents.create(type)
        if (component instanceof SupportsMetaClass) {
            component.setMetaClass(customersDc.entityMetaClass)
        }
        (component as SupportsItemsContainer).setItems(customersDc)
        return component
    }

    /**
     * Creates the component in a separate method so that no local variable of the caller keeps it.
     */
    protected WeakReference<Component> addRemoveAndDrop(ContainerItemsTestView view,
                                                        Class<? extends Component> type) {
        def component = createWithItems(type)
        view.content.add(component)
        view.content.remove(component)
        sendUiResponse()
        return new WeakReference<>(component)
    }

    /**
     * Vaadin keeps a removed component in the UI state tree until the next response is written, so do what
     * writing a response does.
     */
    protected void sendUiResponse() {
        def stateTree = ui.internals.stateTree
        stateTree.runExecutionsBeforeClientResponse()
        stateTree.collectChanges { }
        ui.internals.dumpPendingJavaScriptInvocations()
    }

    protected static AtomicInteger countDataChanges(Component component) {
        def counter = new AtomicInteger()
        (component as SupportsDataProvider).getDataProvider().addDataProviderListener { counter.incrementAndGet() }
        return counter
    }

    protected static long dataViewItemCount(Component component) {
        if (component instanceof JmixVirtualList) {
            // VirtualList has no generic data view
            return component.getDataProvider().size(new Query<>())
        }
        return component.getGenericDataView().getItems().count()
    }

    protected static int containerListenerCount(CollectionContainer<?> container) {
        // EventHub has no public listener count, read its internal map
        Map listeners = container.events.events ?: [:]
        return [CollectionContainer.CollectionChangeEvent, InstanceContainer.ItemPropertyChangeEvent]
                .sum { listeners.get(it)?.length ?: 0 } as int
    }

    protected static boolean isGarbageCollected(WeakReference<?> reference) {
        for (int i = 0; i < 50 && reference.get() != null; i++) {
            System.gc()
            Thread.sleep(10)
        }
        return reference.get() == null
    }

    protected static void collectGarbage() {
        // Keep the sentinel out of an assert: a power assert records its values and keeps them reachable
        def sentinel = new WeakReference<>(new Object())
        boolean collected = isGarbageCollected(sentinel)
        assert collected: "Garbage collection did not run"
    }
}

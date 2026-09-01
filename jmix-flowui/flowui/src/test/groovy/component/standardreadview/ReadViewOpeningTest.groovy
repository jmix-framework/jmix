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

package component.standardreadview

import component.standardreadview.view.CustomerDetailFallbackTestView
import component.standardreadview.view.OrderListTestView
import component.standardreadview.view.OrderReadTestView
import component.standardreadview.view.ReadBlankTestView
import io.jmix.core.DataManager
import io.jmix.flowui.DialogWindows
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ReadViewOpeningTest extends FlowuiTestSpecification {

    @Autowired
    ViewNavigators navigators
    @Autowired
    DialogWindows dialogWindows
    @Autowired
    DataManager dataManager

    Order order

    @Override
    void setup() {
        registerViewBasePackages("component.standardreadview.view")

        order = dataManager.create(Order)
        order.number = 'order-1'
        dataManager.save(order)
    }

    @Override
    void cleanup() {
        dataManager.remove(order)
    }

    def "navigator opens the read view resolved for the entity"() {
        when: "navigating with the read view navigator"
        def origin = navigateToView(ReadBlankTestView)
        navigators.readView(origin, Order)
                .readEntity(order)
                .navigate()

        then: "the read view is shown with the entity loaded"
        def view = UiTestUtils.getCurrentView()
        view instanceof OrderReadTestView
        (view as OrderReadTestView).getEntity().id == order.id
    }

    def "navigator falls back to the detail view opened read-only"() {
        given: "a customer, whose only view in this package is a detail view"
        def customer = dataManager.create(Customer)
        customer.name = 'customer-1'
        dataManager.save(customer)

        when: "navigating with the read view navigator"
        def origin = navigateToView(ReadBlankTestView)
        navigators.readView(origin, Customer)
                .readEntity(customer)
                .navigate()

        then: "the detail view is shown in read-only mode"
        def view = UiTestUtils.getCurrentView()
        view instanceof CustomerDetailFallbackTestView
        (view as CustomerDetailFallbackTestView).isReadOnly()

        cleanup:
        dataManager.remove(customer)
    }

    def "dialog opens the read view resolved for the entity"() {
        when: "opening the read view in a dialog"
        def origin = navigateToView(ReadBlankTestView)
        def dialog = dialogWindows.read(origin, Order)
                .readEntity(order)
                .open()

        then: "the read view shows the entity read through the loader"
        dialog.view instanceof OrderReadTestView
        (dialog.view as OrderReadTestView).getEntity().id == order.id
    }

    def "dialog falls back to the detail view opened read-only"() {
        given: "a customer, whose only view in this package is a detail view"
        def customer = dataManager.create(Customer)
        customer.name = 'customer-1'
        dataManager.save(customer)

        when: "opening it in a dialog with the read builder"
        def origin = navigateToView(ReadBlankTestView)
        def dialog = dialogWindows.read(origin, Customer)
                .readEntity(customer)
                .open()

        then: "the detail view is shown read-only with the entity set"
        dialog.view instanceof CustomerDetailFallbackTestView
        def view = dialog.view as CustomerDetailFallbackTestView
        view.isReadOnly()
        view.getEditedEntity().id == customer.id

        cleanup:
        dataManager.remove(customer)
    }

    def "navigator takes the origin and the entity off a list component"() {
        given: "a list view with the order selected in its grid"
        def listView = navigateToView(OrderListTestView)
        listView.ordersDataGrid.select(listView.ordersDc.getItems().find { it.id == order.id })

        when: "navigating from the grid"
        navigators.readView(listView.ordersDataGrid).navigate()

        then: "the read view shows the selected entity"
        def view = UiTestUtils.getCurrentView()
        view instanceof OrderReadTestView
        (view as OrderReadTestView).getEntity().id == order.id
    }

    def "dialog takes the origin and the entity off a list component"() {
        given: "a list view with the order selected in its grid"
        def listView = navigateToView(OrderListTestView)
        listView.ordersDataGrid.select(listView.ordersDc.getItems().find { it.id == order.id })

        when: "opening a dialog from the grid"
        def dialog = dialogWindows.read(listView.ordersDataGrid).open()

        then: "the read view shows the selected entity"
        dialog.view instanceof OrderReadTestView
        (dialog.view as OrderReadTestView).getEntity().id == order.id
    }

    def "navigator takes the entity off a picker component"() {
        given: "a view with the order set to its picker"
        def listView = navigateToView(OrderListTestView)
        listView.orderPicker.setValue(order)

        when: "navigating from the picker"
        navigators.readView(listView.orderPicker).navigate()

        then: "the read view shows the picker value"
        def view = UiTestUtils.getCurrentView()
        view instanceof OrderReadTestView
        (view as OrderReadTestView).getEntity().id == order.id
    }

    def "dialog takes the entity off a picker component"() {
        given: "a view with the order set to its picker"
        def listView = navigateToView(OrderListTestView)
        listView.orderPicker.setValue(order)

        when: "opening a dialog from the picker"
        def dialog = dialogWindows.read(listView.orderPicker).open()

        then: "the read view shows the picker value"
        dialog.view instanceof OrderReadTestView
        (dialog.view as OrderReadTestView).getEntity().id == order.id
    }

    def "navigator requires an entity"() {
        when: "navigating without an entity"
        def origin = navigateToView(ReadBlankTestView)
        navigators.readView(origin, Order).navigate()

        then:
        thrown(IllegalStateException)
    }
}

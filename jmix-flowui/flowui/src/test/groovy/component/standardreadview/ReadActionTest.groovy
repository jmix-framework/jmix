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
import component.standardreadview.view.CustomerListTestView
import component.standardreadview.view.OrderListTestView
import component.standardreadview.view.OrderReadTestView
import io.jmix.core.DataManager
import io.jmix.flowui.action.list.ReadAction
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ReadActionTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    Order order
    Customer customer

    @Override
    void setup() {
        registerViewBasePackages("component.standardreadview.view")

        order = dataManager.create(Order)
        order.number = 'order-1'
        dataManager.save(order)

        customer = dataManager.create(Customer)
        customer.name = 'customer-1'
        dataManager.save(customer)
    }

    @Override
    void cleanup() {
        dataManager.remove(order)
        dataManager.remove(customer)
    }

    def "action opens the read view when the entity has one"() {
        given: "an order selected in the list"
        def listView = navigateToView(OrderListTestView)
        listView.ordersDataGrid.select(listView.ordersDc.getItems().find { it.id == order.id })

        when: "the read action is performed"
        listView.ordersDataGrid.getAction('read').actionPerform(listView.ordersDataGrid)

        then: "the read view is shown with the entity loaded"
        def view = UiTestUtils.getCurrentView()
        view instanceof OrderReadTestView
        (view as OrderReadTestView).getEntity().id == order.id
    }

    def "action opens the detail view read-only when the entity has no read view"() {
        given: "a customer selected in the list"
        def listView = navigateToView(CustomerListTestView)
        listView.customersDataGrid.select(listView.customersDc.getItems().find { it.id == customer.id })

        when: "the read action is performed"
        listView.customersDataGrid.getAction('read').actionPerform(listView.customersDataGrid)

        then: "the detail view is shown in the read-only mode"
        def view = UiTestUtils.getCurrentView()
        view instanceof CustomerDetailFallbackTestView
        (view as CustomerDetailFallbackTestView).isReadOnly()
    }

    def "action honors an explicitly configured view class"() {
        given: "a customer selected in the list and the view class set on the action"
        def listView = navigateToView(CustomerListTestView)
        listView.customersDataGrid.select(listView.customersDc.getItems().find { it.id == customer.id })

        def action = listView.customersDataGrid.getAction('read') as ReadAction
        action.setViewClass(CustomerDetailFallbackTestView)

        when: "the read action is performed"
        action.actionPerform(listView.customersDataGrid)

        then: "the configured view is opened, read-only as a detail view"
        def view = UiTestUtils.getCurrentView()
        view instanceof CustomerDetailFallbackTestView
        (view as CustomerDetailFallbackTestView).isReadOnly()
    }
}

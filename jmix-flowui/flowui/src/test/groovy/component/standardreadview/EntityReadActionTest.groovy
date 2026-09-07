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

import component.standardreadview.view.CustomerListTestView
import component.standardreadview.view.CustomerDetailFallbackTestView
import component.standardreadview.view.OrderListTestView
import component.standardreadview.view.OrderReadTestView
import io.jmix.core.DataManager
import io.jmix.flowui.kit.component.KeyCombination
import io.jmix.flowui.testassist.UiTestUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(properties = 'jmix.ui.component.picker-read-shortcut = Control-Alt-R')
class EntityReadActionTest extends FlowuiTestSpecification {

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

    def "action opens the read view for the picker value"() {
        given: "a view with the order set to its picker"
        def listView = navigateToView(OrderListTestView)
        listView.orderPicker.setValue(order)

        when: "performing the picker's read action"
        def action = listView.orderPicker.getAction('read')
        action.actionPerform(listView.orderPicker)

        then: "the read view is shown in a dialog with the entity loaded"
        def view = UiTestUtils.getLastOpenedViewDialog()
        view instanceof OrderReadTestView
        (view as OrderReadTestView).getEntity().id == order.id
    }

    def "action opens the fallback detail view read-only"() {
        given: "a customer, whose only view in this package is a detail view"
        def customer = dataManager.create(Customer)
        customer.name = 'customer-1'
        dataManager.save(customer)

        and: "a view with the customer set to its picker"
        def listView = navigateToView(CustomerListTestView)
        listView.customerPicker.setValue(customer)

        when: "performing the picker's read action"
        def action = listView.customerPicker.getAction('read')
        action.actionPerform(listView.customerPicker)

        then: "the detail view is shown read-only"
        def view = UiTestUtils.getLastOpenedViewDialog()
        view instanceof CustomerDetailFallbackTestView
        (view as CustomerDetailFallbackTestView).isReadOnly()

        cleanup:
        dataManager.remove(dataManager.load(Customer).id(customer.id).one())
    }

    def "action takes its shortcut from the application property"() {
        given: "a view with a picker declaring the read action"
        def listView = navigateToView(OrderListTestView)

        expect: "the action carries the configured shortcut"
        listView.orderPicker.getAction('read').shortcutCombination == KeyCombination.create('Control-Alt-R')
    }

    def "action does nothing when the picker is empty"() {
        given: "a view whose picker has no value"
        def listView = navigateToView(OrderListTestView)

        when: "performing the picker's read action"
        listView.orderPicker.getAction('read').actionPerform(listView.orderPicker)

        then: "no dialog is opened"
        UiTestUtils.getOpenedViewDialogs().isEmpty()
    }
}

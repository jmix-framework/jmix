/*
 * Copyright 2020 Haulmont.
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

package model_object_screens

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.RemoveOperation
import io.jmix.ui.ScreenBuilders
import io.jmix.ui.UiConfiguration
import io.jmix.ui.action.Action
import io.jmix.ui.screen.OpenMode
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.model_objects.CustomerObject
import test_support.entity.model_objects.OrderLineObject

import java.time.LocalDate

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class JmixEntityScreensTest extends ScreenSpecification {

    @Autowired
    ScreenBuilders screenBuilders
    @Autowired
    RemoveOperation removeOperation

    @Override
    void setup() {
        exportScreensPackages(['model_object_screens'])
    }

    @Override
    void cleanup() {
        TestJmixEntitiesStorage.instance.clear()
    }

    def "browse and edit CustomerObject"() {
        screens.create("main", OpenMode.ROOT).show()

        when: "empty browser"
        def browser = screens.create(CustomerObjectBrowse)
        browser.show()

        then:
        browser.customerObjectsDc.items.isEmpty()

        when: "create entity"
        def editor = screenBuilders.editor(browser.customerObjectsTable)
                .withScreenClass(CustomerObjectEdit)
                .newEntity()
                .build()
        editor.show()

        then:
        editor.nameField.required

        when:
        editor.nameField.value = 'Joe'
        editor.closeWithCommit()

        then:
        browser.customerObjectsDc.items.size() == 1
        browser.customerObjectsDc.items[0].name == 'Joe'

        when: "edit entity"
        browser.customerObjectsTable.setSelected(browser.customerObjectsDc.items[0])
        editor = screenBuilders.editor(browser.customerObjectsTable)
                .withScreenClass(CustomerObjectEdit)
                .build()
        editor.show()

        then:
        editor.nameField.value == 'Joe'

        when:
        editor.nameField.value = 'Jane'
        editor.closeWithCommit()

        then:
        browser.customerObjectsDc.items.size() == 1
        browser.customerObjectsDc.items[0].name == 'Jane'

        when: "remove entity"
        browser.customerObjectsTable.setSelected(browser.customerObjectsDc.items[0])
        removeOperation.builder(browser.customerObjectsTable)
                .withConfirmation(false)
                .remove()

        then:
        browser.customerObjectsDc.items.isEmpty()
    }

    def "create Order with Customer"() {
        def customer = metadata.create(CustomerObject)
        customer.name = 'Joe'
        TestJmixEntitiesStorage.getInstance().save(customer)

        screens.create("main", OpenMode.ROOT).show()

        when:
        def orderBrowser = screens.create(OrderObjectBrowse)
        orderBrowser.show()

        def orderEditor = screenBuilders.editor(orderBrowser.orderObjectsTable)
                .withScreenClass(OrderObjectEdit)
                .newEntity()
                .build()
        orderEditor.show()

        orderEditor.dateField.value = LocalDate.now()
        orderEditor.numberField.value = '111'

        def customerBrowse = screenBuilders.lookup(orderEditor.customerPicker)
                .withScreenClass(CustomerObjectBrowse)
                .build()
        customerBrowse.show()
        customerBrowse.customerObjectsTable.setSelected(customerBrowse.customerObjectsDc.items[0])
        customerBrowse.select((Action.ActionPerformedEvent) null)

        then:
        orderEditor.customerPicker.value == customer

        when:
        orderEditor.closeWithCommit()

        then:
        orderBrowser.orderObjectsDc.items.size() == 1
        def order = orderBrowser.orderObjectsDc.items[0]
        order.number == '111'
        order.customer == customer
    }

    def "create and edit Order with OrderLines"() {
        screens.create("main", OpenMode.ROOT).show()

        when: "create"
        def orderBrowser = screens.create(OrderObjectBrowse)
        orderBrowser.show()

        def orderEditor = screenBuilders.editor(orderBrowser.orderObjectsTable)
                .withScreenClass(OrderObjectEdit)
                .newEntity()
                .build()
        orderEditor.show()

        orderEditor.dateField.value = LocalDate.now()
        orderEditor.numberField.value = '111'

        def lineEditor = screenBuilders.editor(orderEditor.linesTable)
                .withScreenClass(OrderLineObjectEdit)
                .newEntity()
                .build()
        lineEditor.show()
        lineEditor.productField.value = 'p1'
        lineEditor.quantityField.value = 10d
        lineEditor.closeWithCommit()

        then:
        orderEditor.lineObjectsDc.items.size() == 1
        def orderLine = orderEditor.lineObjectsDc.items[0]
        orderLine.product == 'p1'
        orderLine.quantity == 10d
        TestJmixEntitiesStorage.instance.getAll(OrderLineObject).isEmpty() // because of composition

        when:
        orderEditor.closeWithCommit()

        then:
        orderBrowser.orderObjectsDc.items.size() == 1
        def order = orderBrowser.orderObjectsDc.items[0]
        order.lines.size() == 1
        order.lines[0] == TestJmixEntitiesStorage.instance.getAll(OrderLineObject)[0]

        when: "edit"
        orderBrowser.orderObjectsTable.setSelected(orderBrowser.orderObjectsDc.items[0])
        orderEditor = screenBuilders.editor(orderBrowser.orderObjectsTable)
                .withScreenClass(OrderObjectEdit)
                .build()
        orderEditor.show()

        then:
        orderEditor.lineObjectsDc.items.size() == 1

        when:
        orderEditor.linesTable.setSelected(orderEditor.lineObjectsDc.items[0])
        lineEditor = screenBuilders.editor(orderEditor.linesTable)
                .withScreenClass(OrderLineObjectEdit)
                .build()
        lineEditor.show()
        lineEditor.quantityField.value = 20d
        lineEditor.closeWithCommit()

        orderEditor.closeWithCommit()

        then:
        orderBrowser.orderObjectsDc.items.size() == 1
        orderBrowser.orderObjectsDc.items[0].lines[0].quantity == 20d
    }
}
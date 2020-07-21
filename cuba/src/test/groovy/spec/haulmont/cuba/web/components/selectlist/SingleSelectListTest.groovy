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

package spec.haulmont.cuba.web.components.selectlist

import io.jmix.core.common.event.Subscription
import io.jmix.ui.component.HasValue
import io.jmix.ui.component.SingleSelectList
import io.jmix.ui.screen.OpenMode
import spec.haulmont.cuba.web.UiScreenSpec
import spec.haulmont.cuba.web.components.selectlist.screens.SingleSelectListTestScreen

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class SingleSelectListTest extends UiScreenSpec {

    @SuppressWarnings(['GroovyAssignabilityCheck', 'GroovyAccessibility'])
    void setup() {
        exportScreensPackages(['spec.haulmont.cuba.web.components.selectlist.screens', 'com.haulmont.cuba.web.app.main'])
    }

    def "Value is propagated to ValueSource from SingleOptionsList"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('main', OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = (SingleSelectListTestScreen) screens.create(SingleSelectListTestScreen)
        screen.show()

        when: 'Set value to SingleOptionsList'
        def order = screen.ordersDc.items.get(0)
        screen.singleSelectList.setValue(order)

        then: 'ValueSource is updated'
        screen.orderLineDc.item.order == order
    }

    def "Value is propagated from ValueSource to SingleOptionsList"() {
        def screens = vaadinUi.screens

        def mainWindow = screens.create('main', OpenMode.ROOT)
        screens.show(mainWindow)

        def screen = (SingleSelectListTestScreen) screens.create(SingleSelectListTestScreen)
        screen.show()

        when: 'Set value to ValueSource'
        def order = screen.ordersDc.items.get(0)
        screen.orderLineDc.item.order = order

        then: 'SingleOptionsList is updated'
        screen.singleSelectList.value == order
    }

    def testSetToReadonly() {
        when:
        def component = uiComponents.create(SingleSelectList)

        component.setEditable(false)

        then:
        !component.editable

        when:

        component.setOptionsList(new ArrayList<>(Arrays.asList("One", "Two", "Three")))
        component.setValue("One")

        then:
        component.value == 'One'
        !component.editable
    }

    def testSetToReadonlyFromValueListener() {
        when:
        def component = uiComponents.create(SingleSelectList)

        then:
        component.editable

        when:
        component.addValueChangeListener({ e -> component.setEditable(false) })

        component.setOptionsList(["One", "Two", "Three"])
        component.setValue("One")

        then:
        component.value == 'One'
        !component.editable
    }

    def testValueChangeListener() {
        when:
        def component = uiComponents.create(SingleSelectList)

        final AtomicInteger counter = new AtomicInteger(0)

        then:
        component.value == null

        when:

        Consumer<HasValue.ValueChangeEvent> listener1 = { e ->
            counter.addAndGet(1)
        }
        Subscription subscription = component.addValueChangeListener(listener1)

        component.setOptionsList(new ArrayList<>(Arrays.asList("One", "Two", "Three")))
        component.setValue("Two")

        subscription.remove()

        then:
        counter.get() == 1

        when:

        Consumer<HasValue.ValueChangeEvent> listener2 = { e ->
            counter.addAndGet(1)
        }
        subscription = component.addValueChangeListener(listener2)

        component.setValue("One")

        then:
        component.value == 'One'
        counter.get() == 2

        when:

        subscription.remove()
        Consumer<HasValue.ValueChangeEvent> listener3 = { e ->
            counter.addAndGet(1)
        }
        component.addValueChangeListener(listener3)

        component.setValue("Three")

        then:
        counter.get() == 3
    }
}

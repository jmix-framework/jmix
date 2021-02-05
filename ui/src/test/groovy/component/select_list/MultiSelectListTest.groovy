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

package component.select_list

import component.select_list.screen.MultiSelectListTestScreen
import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.MultiSelectList
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.UiTestConfiguration
import test_support.entity.sales.OrderLine
import test_support.entity.sales.Product

import java.util.function.Consumer

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class MultiSelectListTest extends ScreenSpecification {

    @Override
    void setup() {
        exportScreensPackages(["component.select_list"])
    }

    def 'List value is propagated to ValueSource from MultiOptionsList'() {
        showTestMainScreen()

        def screen = screens.create(MultiSelectListTestScreen)
        screen.show()

        def optionsList = screen.selectList as MultiSelectList<OrderLine>
        def orderLine = screen.allOrderLinesDc.getItems().get(0)
        def orderLinesDc = screen.orderLinesDc

        when: 'List value is set to MultiOptionsList'
        optionsList.setValue([orderLine])

        then: 'ValueSource is updated'
        orderLinesDc.items.size() == 1 && orderLinesDc.items.contains(orderLine)
    }

    def 'List value is propagated to MultiOptionsList from ValueSource'() {
        showTestMainScreen()

        def screen = screens.create(MultiSelectListTestScreen)
        screen.show()

        def optionsList = screen.selectList as MultiSelectList<OrderLine>
        def orderLine = screen.allOrderLinesDc.getItems().get(0)

        when: 'List value is set to ValueSource'
        screen.orderLinesDc.mutableItems.add(orderLine)

        then: 'OptionsList is updated'
        optionsList.value.size() == 1 && optionsList.value.contains(orderLine)
    }

    def 'Set value is propagated to ValueSource from MultiOptionsList'() {
        showTestMainScreen()

        def screen = screens.create(MultiSelectListTestScreen)
        screen.show()

        def optionsList = screen.setSelectList as MultiSelectList<Product>
        def product = screen.allProductsDc.items.get(0)
        def catalog = screen.catalogDc.item

        when: 'Set value is set to MultiOptionsList'
        optionsList.setValue(Collections.singleton(product))

        then: 'ValueSource is updated'
        catalog.products.size() == 1 && catalog.products.contains(product)
    }

    def 'ValueChangeEvent is fired exactly once for MultiOptionsList'() {
        showTestMainScreen()

        def screen = screens.create(MultiSelectListTestScreen)
        screen.show()

        def optionsList = screen.selectList as MultiSelectList<OrderLine>
        def requiredOptionsList = screen.requiredSelectList as MultiSelectList<OrderLine>

        def valueChangeListener = Mock(Consumer)
        def requiredValueChangeListener = Mock(Consumer)

        optionsList.addValueChangeListener(valueChangeListener)
        requiredOptionsList.addValueChangeListener(requiredValueChangeListener)

        def order = screen.orderDc.item
        def orderLine = screen.orderLineDc.item

        def olOption = screen.allOrderLinesDc.items.get(0)
        def secondOlOption = screen.allOrderLinesDc.items.get(1)

        when: 'A value is set to MultiOptionsList'
        optionsList.setValue([olOption])

        then: 'ValueChangeEvent is fired once'
        1 * valueChangeListener.accept(Specification._)
        1 * requiredValueChangeListener.accept(Specification._)

        when: 'ValueSource is changed'
        screen.orderLinesDc.mutableItems.add(secondOlOption)

        then: 'ValueChangeEvent is fired once'
        1 * valueChangeListener.accept(Specification._)
        1 * requiredValueChangeListener.accept(Specification._)

        when: 'Entity property value is set to null'
        order.orderLines = null
        orderLine.product = null

        then: 'ValueChangeEvent is fired once'
        1 * valueChangeListener.accept(Specification._)
        1 * requiredValueChangeListener.accept(Specification._)
    }
}

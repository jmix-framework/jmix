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

package screen

import com.google.common.collect.Iterables
import io.jmix.core.CoreConfiguration
import io.jmix.core.TimeSource
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.ScreenBuilders
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.HasValue
import io.jmix.ui.model.CollectionContainer
import io.jmix.ui.screen.LookupScreen
import io.jmix.ui.testassist.spec.ScreenSpecification
import io.jmix.ui.util.OperationResult
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Customer
import test_support.entity.sales.Order
import test_support.entity.sales.screen.OrderBrowse
import test_support.entity.sales.screen.OrderEdit

import java.util.function.Consumer
import java.util.function.Function

@SuppressWarnings(["GroovyAccessibility", "GroovyAssignabilityCheck"])
@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ScreenBuildersTest extends ScreenSpecification {

    @Autowired
    ScreenBuilders screenBuilders
    @Autowired
    TimeSource timeSource

    @Override
    def setup() {
        exportScreensPackages(['test_support.entity.sales.screen'])
    }

    def "build and show UserEditor with ScreenBuilders"() {

        def afterCloseListener = Mock(Consumer)
        def transformation = Spy(new Function() {
            @Override
            Object apply(Object o) {
                return o
            }
        })

        def field = Mock(HasValue)

        when:
        def mainScreen = showTestMainScreen()

        then:
        vaadinUi.topLevelWindow == mainScreen.window

        when:
        def order = metadata.create(Order)
        def customer = metadata.create(Customer)
        customer.setName("test customer")
        order.setNumber("test number")
        order.setCustomer(customer)

        OrderEdit editor = (OrderEdit) screenBuilders.editor(Order, mainScreen)
                .newEntity(order)
                .withTransformation(transformation)
                .withField(field)
                .show()

        editor.amountField.setValue(new BigDecimal(10))
        editor.dateField.setValue(timeSource.currentTimestamp())

        editor.addAfterCloseListener(afterCloseListener)
        def result = editor.closeWithCommit()

        then:
        result.status == OperationResult.Status.SUCCESS

        1 * transformation.apply(_) >> { Order selectedOrder ->
            assert selectedOrder != null
        }
        1 * afterCloseListener.accept(_)
        1 * field.setValue(_)
    }

    def "build and show UserBrowser with ScreenBuilders"() {
        def afterCloseListener = Mock(Consumer)
        def transformation = Spy(new Function() {
            @Override
            Object apply(Object o) {
                return o
            }
        })

        def field = Mock(HasValue)

        when:
        def mainScreen = showTestMainScreen()

        then:
        vaadinUi.topLevelWindow == mainScreen.window

        when:
        OrderBrowse lookup = (OrderBrowse) screenBuilders.lookup(Order, mainScreen)
                .withScreenId('test_Order.browse')
                .withTransformation(transformation)
                .withField(field)
                .show()

        lookup.addAfterCloseListener(afterCloseListener)

        def ordersDc = lookup.screenData.getContainer('ordersDc') as CollectionContainer
        def order = Iterables.getLast(ordersDc.getItems()) as Order
        lookup.ordersTable.setSelected(order)

        def selectAction = lookup.window.getAction(LookupScreen.LOOKUP_SELECT_ACTION_ID)
        selectAction.actionPerform(lookup.ordersTable)

        then:
        !screens.getOpenedScreens().all.contains(lookup)

        1 * transformation.apply(_)
        1 * afterCloseListener.accept(_)
        1 * field.setValue(_)
    }
}
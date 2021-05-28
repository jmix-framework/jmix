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

package data_context

import data_context.screen.OrderScreen
import io.jmix.core.CoreConfiguration
import io.jmix.core.DataManager
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.screen.UiControllerUtils
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.ContextConfiguration
import spock.lang.Unroll
import test_support.UiTestConfiguration
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.OrderLineParam

@SuppressWarnings("GroovyAssignabilityCheck")
@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class CompositionScreensTest extends ScreenSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        exportScreensPackages(['data_context.screen'])
    }

    @Override
    void cleanup() {
        jdbcTemplate.update("delete from TEST_ORDER_LINE_PARAM")
        jdbcTemplate.update("delete from TEST_ORDER_LINE")
        jdbcTemplate.update("delete from TEST_ORDER")
    }

    @Unroll
    def "create and immediate edit of the same nested instance"(boolean explicitParentDc) {

        showTestMainScreen()

        def orderScreen = screens.create(OrderScreen)
        def order = metadata.create(Order)
        orderScreen.order = order
        orderScreen.show()

        def orderScreenDc = UiControllerUtils.getScreenData(orderScreen).dataContext

        when: "create entity"

        def lineScreenForCreate = orderScreen.buildLineScreenForCreate(explicitParentDc)
        lineScreenForCreate.show()

        lineScreenForCreate.changeCommitAndClose(1)

        then:

        def order1 = orderScreenDc.find(Order, order.id)
        order1.orderLines.size() == 1
        def line1 = order1.orderLines[0]
        line1.order.is(order1)

        when: "edit same entity"

        def lineScreenForEdit = orderScreen.buildLineScreenForEdit(explicitParentDc)
        lineScreenForEdit.show()

        lineScreenForEdit.changeCommitAndClose(2)

        then:

        def order2 = orderScreenDc.find(Order, order.id)
        order2.is(order1)
        order2.orderLines.size() == 1
        def line2 = order2.orderLines[0]
        line2.is(line1)
        line2.order.is(order2)

        where:

        explicitParentDc << [true, false]
    }

    def "remove nested instance on 2nd level"() {

        showTestMainScreen()

        def orderScreen = screens.create(OrderScreen)

        def order = dataManager.save(new Order(number: '1', orderLines: []))

        def orderLine = dataManager.save(new OrderLine(quantity: 1, params: []))
        orderLine.order = order
        order.orderLines.add(orderLine)

        def lineParam = dataManager.save(new OrderLineParam(name: 'p1'))
        lineParam.orderLine = orderLine
        orderLine.params.add(lineParam)

        orderScreen.order = order
        orderScreen.show()

        def orderScreenCtx = UiControllerUtils.getScreenData(orderScreen).dataContext

        when:

        def lineScreen = orderScreen.buildLineScreenForEdit(false)
        lineScreen.show()

        def lineScreenCtx = UiControllerUtils.getScreenData(lineScreen).dataContext

        then:

        lineScreenCtx.parent == orderScreenCtx
        lineScreen.paramsDc.items.contains(lineParam)

        when:

        def lineParam1 = lineScreenCtx.find(lineParam)
        lineScreen.paramsDc.getMutableItems().remove(lineParam1)
        lineScreenCtx.remove(lineParam1)
        lineScreenCtx.commit()

        then:

        orderScreenCtx.isRemoved(lineParam)

        cleanup:

        dataManager.remove(lineParam, orderLine, order)
    }

    def "remove nested instance on 2nd level if the root entity did not have the full object graph"() {

        showTestMainScreen()

        def orderScreen = screens.create(OrderScreen)

        def order = dataManager.save(new Order(number: '1', orderLines: []))

        OrderLine orderLine = dataManager.save(new OrderLine(quantity: 1, params: [], order: order))
        order.orderLines.add(orderLine)

        orderScreen.order = order
        orderScreen.show()

        def orderScreenCtx = UiControllerUtils.getScreenData(orderScreen).dataContext

        when:

        def lineScreen = orderScreen.buildLineScreenForEdit(false)

        def lineParam = dataManager.save(new OrderLineParam(name: 'p1'))
        lineScreen.getEditedEntity().params = []
        lineScreen.getEditedEntity().params.add(lineParam)

        lineScreen.show()

        def lineScreenCtx = UiControllerUtils.getScreenData(lineScreen).dataContext

        then:

        lineScreenCtx.parent == orderScreenCtx
        lineScreen.paramsDc.items.contains(lineParam)

        when:

        def lineParam1 = lineScreenCtx.find(lineParam)
        lineScreen.paramsDc.getMutableItems().remove(lineParam1)
        lineScreenCtx.remove(lineParam1)
        lineScreenCtx.commit()

        then:

        orderScreenCtx.isRemoved(lineParam)

        cleanup:

        dataManager.remove(lineParam, orderLine, order)
    }
}

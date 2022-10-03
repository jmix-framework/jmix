/*
 * Copyright (c) 2020 Haulmont.
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

import io.jmix.core.Metadata
import io.jmix.flowui.UiComponents
import io.jmix.flowui.model.DataComponents
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.entity.sales.OrderLineParam
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ContainersWithDataContextTest extends FlowuiTestSpecification {

    @Autowired
    DataComponents dataComponents
    @Autowired
    UiComponents uiComponents
    @Autowired
    Metadata metadata

    def "entity added to property container are not merged into context"() {

        def context = dataComponents.createDataContext()
        def orderDc = dataComponents.createInstanceContainer(Order)
        def linesDc = dataComponents.createCollectionContainer(OrderLine, orderDc, 'orderLines')

        def order1 = metadata.create(Order)
        order1.number = 1
        order1.orderLines = []

        def line1 = metadata.create(OrderLine)
        line1.order = order1
        line1.quantity = 1

        order1.orderLines.add(line1)

        orderDc.setItem(context.merge(order1))

        def line2 = metadata.create(OrderLine)
        line2.order = order1
        line2.quantity = 2

        when:

        linesDc.getMutableItems().add(line2)

        then:

        !context.contains(line2)
    }

    def "nested collection property containers"() {
        def orderContext = dataComponents.createDataContext()
        def lineContext
        def paramContext

        def orderDc = dataComponents.createInstanceContainer(Order)
        def linesDc = dataComponents.createCollectionContainer(OrderLine, orderDc, 'orderLines')
        def paramsDc = dataComponents.createCollectionContainer(OrderLineParam, linesDc, 'params')

        def order1 = orderContext.create(Order)
        order1.number = 1
        order1.orderLines = []

        orderDc.setItem(order1)

        when:

        // first OrderLine and OrderLineParam

        def line1 = metadata.create(OrderLine)
        line1.order = orderDc.getItem()

        lineContext = dataComponents.createDataContext()
        lineContext.setParent(orderContext)

        line1 = lineContext.merge(line1)
        line1.quantity = 1
        lineContext.save()
        line1 = orderContext.find(line1)

        linesDc.mutableItems.add(line1)
        linesDc.setItem(line1)

        def param1 = metadata.create(OrderLineParam)
        param1.orderLine = linesDc.getItem()

        paramContext = dataComponents.createDataContext()
        paramContext.setParent(orderContext)

        param1 = paramContext.merge(param1)
        param1.name = "p1"
        paramContext.save()
        param1 = orderContext.find(param1)

        paramsDc.mutableItems.add(param1)

        then:

        order1.orderLines.size() == 1
        order1.orderLines[0].params.size() == 1
        order1.orderLines[0].params[0] == param1
        paramsDc.items.size() == 1

        when:

        // second OrderLine and OrderLineParam

        def line2 = metadata.create(OrderLine)
        line2.order = orderDc.getItem()

        lineContext = dataComponents.createDataContext()
        lineContext.setParent(orderContext)

        line2 = lineContext.merge(line2)
        line2.quantity = 2
        lineContext.save()
        line2 = orderContext.find(line2)

        linesDc.mutableItems.add(line2)
        linesDc.setItem(line2)

        def param2 = metadata.create(OrderLineParam)
        param2.orderLine = linesDc.getItem()

        paramContext = dataComponents.createDataContext()
        paramContext.setParent(orderContext)

        param2 = paramContext.merge(param2)
        param2.name = "p2"
        paramContext.save()
        param2 = orderContext.find(param2)

        paramsDc.mutableItems.add(param2)

        then:

        order1.orderLines.size() == 2
        order1.orderLines[1].params.size() == 1
        order1.orderLines[1].params[0] == param2
        paramsDc.items.size() == 1

        when:

        linesDc.setItem(null)
        order1.orderLines[1].params = []

        then:
        noExceptionThrown()
    }
}

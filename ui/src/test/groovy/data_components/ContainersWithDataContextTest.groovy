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


import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class ContainersWithDataContextTest extends ScreenSpecification {

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
}

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

import component.standardreadview.view.OrderCompositionReadTestView
import component.standardreadview.view.OrderLineDetailFallbackTestView
import component.standardreadview.view.ReadBlankTestView
import io.jmix.core.DataManager
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.model.impl.NoopDataContext
import io.jmix.flowui.testassist.UiTestUtils
import io.jmix.flowui.view.ViewControllerUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.entity.sales.OrderLine
import test_support.spec.FlowuiTestSpecification

/**
 * A read view of an entity with a composition collection: the grid's {@code list_read} action must open
 * the line, even though the read view's own {@code DataContext} is a {@link NoopDataContext} that cannot
 * be a parent of the opened view's context.
 */
@SpringBootTest
class ReadActionCompositionTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager
    @Autowired
    ViewNavigators navigators

    Order order
    OrderLine line

    @Override
    void setup() {
        registerViewBasePackages("component.standardreadview.view")

        order = dataManager.create(Order)
        order.number = 'order-with-lines'

        line = dataManager.create(OrderLine)
        line.order = order
        line.description = 'line-1'
        line.quantity = 2

        dataManager.save(order, line)
    }

    @Override
    void cleanup() {
        dataManager.remove(line)
        dataManager.remove(order)
    }

    def "read action of a composition grid opens the line from a read view"() {
        given: "the order read view is open"
        def origin = navigateToView(ReadBlankTestView)
        navigators.readView(origin, Order)
                .withViewClass(OrderCompositionReadTestView)
                .readEntity(order)
                .navigate()

        def readView = UiTestUtils.currentView as OrderCompositionReadTestView

        and: "its data context cannot be a parent of another one"
        ViewControllerUtils.getViewData(readView).dataContextOrNull instanceof NoopDataContext

        and: "a line is selected in the composition grid"
        readView.linesDataGrid.select(readView.linesDc.items.find { it.id == line.id })

        when: "the read action of the grid is performed"
        readView.linesDataGrid.getAction('read').actionPerform(readView.linesDataGrid)

        then: "the line is shown in a dialog, read-only, instead of failing on the parent data context"
        def dialogView = UiTestUtils.lastOpenedViewDialog
        dialogView instanceof OrderLineDetailFallbackTestView
        (dialogView as OrderLineDetailFallbackTestView).readOnly
        (dialogView as OrderLineDetailFallbackTestView).editedEntity.id == line.id
    }
}

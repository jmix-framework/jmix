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

import component.standardreadview.view.NoContainerReadTestView
import component.standardreadview.view.NoLoaderReadTestView
import component.standardreadview.view.OrderReadTestView
import component.standardreadview.view.ReadBlankTestView
import io.jmix.core.DataManager
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.model.impl.NoopDataContext
import io.jmix.flowui.testassist.UiTestUtils
import io.jmix.flowui.view.ViewControllerUtils
import io.jmix.flowui.view.navigation.RouteSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class StandardReadViewTest extends FlowuiTestSpecification {

    @Autowired
    ViewNavigators navigators
    @Autowired
    DataManager dataManager
    @Autowired
    RouteSupport routeSupport

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

    protected OrderReadTestView navigateToReadView(Object entityId) {
        def origin = navigateToView(ReadBlankTestView)
        navigators.view(origin, OrderReadTestView)
                .withRouteParameters(routeSupport.createRouteParameters('id', entityId))
                .navigate()

        UiTestUtils.getCurrentView() as OrderReadTestView
    }

    def "read view always gets a read-only data context"() {
        when: "navigating to a read view whose descriptor does not declare readOnly"
        def view = navigateToReadView(order.id)
        def dataContext = ViewControllerUtils.getViewData(view).getDataContext()

        then: "the context is the no-op one"
        dataContext instanceof NoopDataContext

        when: "an attribute of the shown entity is changed"
        view.getEntity().number = 'changed'

        then: "nothing is tracked"
        !dataContext.hasChanges()
        !dataContext.isModified(view.getEntity())
    }

    def "read view without @ReadEntityContainer fails with a meaningful message"() {
        when: "navigating to a read view that declares no container"
        def origin = navigateToView(ReadBlankTestView)
        navigators.view(origin, NoContainerReadTestView)
                .withRouteParameters(routeSupport.createRouteParameters('id', order.id))
                .navigate()

        then: "the failure names the annotation"
        def e = thrown(IllegalStateException)
        e.message.contains('ReadEntityContainer')
    }

    def "read view whose container has no loader fails with a meaningful message"() {
        when: "navigating to a read view whose container declares no loader"
        def origin = navigateToView(ReadBlankTestView)
        navigators.view(origin, NoLoaderReadTestView)
                .withRouteParameters(routeSupport.createRouteParameters('id', order.id))
                .navigate()

        then: "the failure names the loader"
        def e = thrown(IllegalStateException)
        e.message.contains('Loader')
    }

    def "read view loads the entity by the route id"() {
        when: "navigating to a read view with an entity id in the route"
        def view = navigateToReadView(order.id)

        then: "the entity is loaded into the container"
        view.getEntity().id == order.id
        view.getEntity().number == 'order-1'
    }
}

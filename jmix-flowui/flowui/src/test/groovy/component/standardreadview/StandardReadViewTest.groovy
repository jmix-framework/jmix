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
import io.jmix.flowui.DialogWindows
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.component.textfield.TypedTextField
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
    DialogWindows dialogWindows
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

    def "data-bound components are read-only, unbound ones are untouched"() {
        when: "navigating to a read view"
        def view = navigateToReadView(order.id)

        then: "the bound field is read-only and the unbound one is not"
        (UiComponentUtils.getComponent(view, 'numberField') as TypedTextField).isReadOnly()
        !(UiComponentUtils.getComponent(view, 'plainField') as TypedTextField).isReadOnly()
    }

    def "components are read-only when the view is opened in a dialog"() {
        when: "the read view is opened in a dialog"
        def origin = navigateToView(ReadBlankTestView)
        def dialog = dialogWindows.view(origin, OrderReadTestView)
                .withViewConfigurer { OrderReadTestView it -> it.setEntityToRead(order) }
                .open()
        OrderReadTestView view = dialog.view

        then: "the bound field is read-only there as well"
        (UiComponentUtils.getComponent(view, 'numberField') as TypedTextField).isReadOnly()
    }

    def "navigating to another id re-reads the entity"() {
        given: "a second stored order"
        def another = dataManager.create(Order)
        another.number = 'order-2'
        dataManager.save(another)

        and: "a read view opened on the first one"
        def first = navigateToReadView(order.id)
        def firstInstance = first

        when: "navigating to the same view with another id"
        def second = navigateToReadView(another.id)

        then: "the shown entity is the second one"
        second.getEntity().id == another.id
        second.getEntity().number == 'order-2'

        cleanup:
        dataManager.remove(another)
    }

    def "entity set through setEntityToRead is re-read through the loader"() {
        given: "an instance whose state differs from the stored one"
        def detached = dataManager.load(Order).id(order.id).one()
        detached.number = 'stale'

        when: "the read view is opened in a dialog with that instance"
        def origin = navigateToView(ReadBlankTestView)
        def dialog = dialogWindows.view(origin, OrderReadTestView)
                .withViewConfigurer { OrderReadTestView it -> it.setEntityToRead(detached) }
                .open()
        OrderReadTestView view = dialog.view

        then: "the shown entity comes from the loader, not from the passed instance"
        !view.getEntity().is(detached)
        view.getEntity().number == 'order-1'
    }

    def "the passed entity is returned before the load"() {
        given: "a read view instance that has not been shown yet"
        def origin = navigateToView(ReadBlankTestView)
        def dialog = dialogWindows.view(origin, OrderReadTestView).build()
        OrderReadTestView view = dialog.view

        when: "an entity is passed to it"
        view.setEntityToRead(order)

        then: "getEntity returns that instance while the container is still empty"
        view.getEntity().is(order)
    }

    def "setEntityToRead rejects an entity without an id"() {
        given: "a read view instance"
        def origin = navigateToView(ReadBlankTestView)
        def dialog = dialogWindows.view(origin, OrderReadTestView).build()

        when: "an entity with no id is passed"
        def unsaved = dataManager.create(Order)
        unsaved.setId(null)
        (dialog.view as OrderReadTestView).setEntityToRead(unsaved)

        then:
        thrown(IllegalArgumentException)
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

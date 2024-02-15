/*
 * Copyright 2022 Haulmont.
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

package component.standarddetailview

import com.vaadin.flow.component.UI
import com.vaadin.flow.router.RouteParameters
import component.standarddetailview.view.BlankTestView
import component.standarddetailview.view.OrderDetailTestView
import component.standarddetailview.view.TestCopyingSystemStateDetailTestView
import io.jmix.core.DataManager
import io.jmix.core.Metadata
import io.jmix.flowui.DialogWindows
import io.jmix.flowui.ViewNavigators
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.TestCopyingSystemStateEntity
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class StandardDetailViewTest extends FlowuiTestSpecification {

    @Autowired
    protected ViewNavigators navigators
    @Autowired
    protected DialogWindows dialogWindows;

    @Autowired
    protected Metadata metadata

    @Autowired
    protected DataManager dataManager

    protected Order orderToEdit

    @Override
    void setup() {
        registerViewBasePackages("component.standarddetailview.view")

        orderToEdit = dataManager.create(Order)
        dataManager.save(orderToEdit)
    }

    @Override
    void cleanup() {
        dataManager.remove(orderToEdit)
    }

    def "Edit DTO entity in standard detail view"() {
        when: "Edit DTO entity in standard detail view"

        def entity = metadata.create(TestCopyingSystemStateEntity)
        entity.setName("test")

        navigators.detailView(TestCopyingSystemStateEntity)
                .withViewClass(TestCopyingSystemStateDetailTestView)
                .withBackwardNavigation(false)
                .withRouteParameters(RouteParameters.empty())
                .withAfterNavigationHandler {event ->
                    event.view.setEntityToEdit(entity)
                }
                .navigate()

        then: """
              Editor should have the same entity instance
              """

        TestCopyingSystemStateDetailTestView detailView
                = UI.getCurrent().getInternals().getActiveRouterTargetsChain().get(0)

        entity.name == detailView.getEditedEntity().name
    }

    def "Edit DTO entity in standard detail view with dialog mode"() {
        when: "Edit DTO entity in standard detail view with dialog mode"

        def origin = navigateToView(BlankTestView)

        dialogWindows.detail(origin, TestCopyingSystemStateEntity)
                .withViewClass(TestCopyingSystemStateDetailTestView)
                .editEntity(metadata.create(TestCopyingSystemStateEntity))
                .open()

        then: """
              No exceptions should be thrown because Dialog sets entity instance
              as is and does not reload it
              """
        noExceptionThrown()
    }

    def "Edit JPA entity in standard detail view"() {
        when: "Edit JPA entity in standard detail view"

        navigators.detailView(Order)
                .editEntity(orderToEdit)
                .withViewClass(OrderDetailTestView)
                .withBackwardNavigation(false)
                .navigate()

        then: """
              No exceptions should be thrown.
              """

        noExceptionThrown()
    }
}

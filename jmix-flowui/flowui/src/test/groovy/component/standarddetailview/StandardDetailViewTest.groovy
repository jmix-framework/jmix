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
import io.jmix.core.Id
import io.jmix.core.Metadata
import io.jmix.flowui.DialogWindows
import io.jmix.flowui.ViewNavigators
import io.jmix.flowui.model.DataComponents
import io.jmix.flowui.model.DataContext
import io.jmix.flowui.testassist.UiTestUtils
import io.jmix.flowui.view.DialogWindow
import io.jmix.flowui.view.StandardOutcome
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
    protected DialogWindows dialogWindows

    @Autowired
    protected Metadata metadata

    @Autowired
    protected DataManager dataManager

    @Autowired
    protected DataComponents dataComponents

    protected Order orderToEdit

    @Override
    void setup() {
        registerViewBasePackages("component.standarddetailview.view")

        orderToEdit = dataManager.create(Order)
        dataManager.save(orderToEdit)
    }

    @Override
    void cleanup() {
        // The order may be saved by a test, so remove it by id to avoid a stale version.
        dataManager.remove(Id.of(orderToEdit))
    }

    def "Edit DTO entity in standard detail view"() {
        when: "Edit DTO entity in standard detail view"

        def entity = metadata.create(TestCopyingSystemStateEntity)
        entity.setName("test")

        def origin = navigateToView(BlankTestView)
        navigators.detailView(origin, TestCopyingSystemStateEntity)
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

        def origin = navigateToView(BlankTestView)
        navigators.detailView(origin, Order)
                .editEntity(orderToEdit)
                .withViewClass(OrderDetailTestView)
                .withBackwardNavigation(false)
                .navigate()

        then: """
              No exceptions should be thrown.
              """

        noExceptionThrown()
    }

    def "existing entity: editing then reverting a scalar clears the unsaved-changes prompt (#1409)"() {
        given: "an Order detail view opened on an existing entity"
        def origin = navigateToView(BlankTestView)
        navigators.detailView(origin, Order)
                .editEntity(orderToEdit)
                .withViewClass(OrderDetailTestView)
                .withBackwardNavigation(false)
                .navigate()
        OrderDetailTestView view = UiTestUtils.getCurrentView()
        Order edited = view.getEditedEntity()
        def original = edited.number

        when: "a scalar is edited and then set back to its original value"
        edited.number = 'temp-changed'
        edited.number = original

        then: "the edit reverted, so the view reports no unsaved changes"
        !view.hasUnsavedChanges()
    }

    def "existing entity: an outstanding scalar edit still reports unsaved changes"() {
        given: "an Order detail view opened on an existing entity"
        def origin = navigateToView(BlankTestView)
        navigators.detailView(origin, Order)
                .editEntity(orderToEdit)
                .withViewClass(OrderDetailTestView)
                .withBackwardNavigation(false)
                .navigate()
        OrderDetailTestView view = UiTestUtils.getCurrentView()
        Order edited = view.getEditedEntity()

        when: "a scalar is edited and left changed"
        edited.number = 'temp-changed'

        then: "the outstanding edit still reports unsaved changes"
        view.hasUnsavedChanges()
    }

    def "create view: an untouched new entity reports no unsaved changes"() {
        given: "an Order detail view opened to create a new entity"
        def origin = navigateToView(BlankTestView)
        navigators.detailView(origin, Order)
                .newEntity()
                .withViewClass(OrderDetailTestView)
                .withBackwardNavigation(false)
                .navigate()
        OrderDetailTestView view = UiTestUtils.getCurrentView()

        expect: "nothing was edited after opening, so there are no unsaved changes"
        !view.hasUnsavedChanges()
    }

    def "create view: editing the new entity reports unsaved changes"() {
        given: "an Order detail view opened to create a new entity"
        def origin = navigateToView(BlankTestView)
        navigators.detailView(origin, Order)
                .newEntity()
                .withViewClass(OrderDetailTestView)
                .withBackwardNavigation(false)
                .navigate()
        OrderDetailTestView view = UiTestUtils.getCurrentView()
        Order edited = view.getEditedEntity()

        when: "a scalar of the new entity is edited"
        edited.number = 'n1'

        then: "the view reports unsaved changes"
        view.hasUnsavedChanges()
    }

    def "create view: a new entity kept in the modified set with no attribute edits still reports unsaved changes"() {
        given: "an Order detail view opened to create a new entity"
        def origin = navigateToView(BlankTestView)
        navigators.detailView(origin, Order)
                .newEntity()
                .withViewClass(OrderDetailTestView)
                .withBackwardNavigation(false)
                .navigate()
        OrderDetailTestView view = UiTestUtils.getCurrentView()
        Order edited = view.getEditedEntity()
        DataContext dataContext = view.getViewData().getDataContext()

        when: "an edit sets the modifiedAfterOpen latch and is then reverted (which un-tracks the attribute), but the new entity is explicitly kept modified with no tracked attributes"
        def original = edited.number
        edited.number = 'temp-changed'
        edited.number = original
        dataContext.setModified(edited, true)

        then: "the new entity is in the modified set with no modified attributes, and the view still reports unsaved changes (save() would persist it)"
        dataContext.getModifiedAttributes(edited).isEmpty()
        !dataContext.getModified().isEmpty()
        view.hasUnsavedChanges()
    }

    def "dialog: closing by the close button after save closes with SAVE and updates the list container (#5706)"() {
        given: "an Order detail view opened in a dialog from a list container"
        def origin = navigateToView(BlankTestView)
        def ordersDc = dataComponents.createCollectionContainer(Order)
        ordersDc.setItems([dataManager.load(Id.of(orderToEdit)).one()])

        def outcomes = []
        DialogWindow<OrderDetailTestView> dialog = dialogWindows.detail(origin, Order)
                .withViewClass(OrderDetailTestView)
                .withContainer(ordersDc)
                .editEntity(ordersDc.getItems().get(0))
                .withAfterCloseListener { event -> outcomes << outcomeOf(event) }
                .build()
        dialog.open()
        OrderDetailTestView view = dialog.getView()

        when: "the entity is saved without closing and then the view is closed with the default action"
        view.getEditedEntity().number = 'saved-in-dialog'
        view.save()
        view.closeWithDefaultAction()

        then: "the dialog reports the SAVE outcome and the list container shows the saved value"
        outcomes == [StandardOutcome.SAVE]
        ordersDc.getItems().get(0).number == 'saved-in-dialog'
    }

    def "close(CLOSE) after save: both close events carry SAVE (#5706)"() {
        given: "an Order detail view opened in a dialog"
        def origin = navigateToView(BlankTestView)
        DialogWindow<OrderDetailTestView> dialog = dialogWindows.detail(origin, Order)
                .withViewClass(OrderDetailTestView)
                .editEntity(orderToEdit)
                .build()
        dialog.open()
        OrderDetailTestView view = dialog.getView()

        def beforeCloseOutcomes = []
        def afterCloseOutcomes = []
        view.addBeforeCloseListener { event -> beforeCloseOutcomes << outcomeOf(event) }
        view.addAfterCloseListener { event -> afterCloseOutcomes << outcomeOf(event) }

        when: "the entity is saved and the view is closed with CLOSE, as Tabbed Mode does when a tab is closed"
        view.getEditedEntity().number = 'saved'
        view.save()
        view.close(StandardOutcome.CLOSE)

        then: "the outcome is replaced with SAVE before the view starts closing"
        beforeCloseOutcomes == [StandardOutcome.SAVE]
        afterCloseOutcomes == [StandardOutcome.SAVE]
    }

    def "close(CLOSE) without save keeps the CLOSE outcome"() {
        given: "an Order detail view opened in a dialog"
        def origin = navigateToView(BlankTestView)
        def outcomes = []
        DialogWindow<OrderDetailTestView> dialog = dialogWindows.detail(origin, Order)
                .withViewClass(OrderDetailTestView)
                .editEntity(orderToEdit)
                .withAfterCloseListener { event -> outcomes << outcomeOf(event) }
                .build()
        dialog.open()

        when: "the view is closed with the default action without saving"
        dialog.getView().closeWithDefaultAction()

        then: "the outcome stays CLOSE"
        outcomes == [StandardOutcome.CLOSE]
    }

    def "close(DISCARD) after save keeps the DISCARD outcome"() {
        given: "an Order detail view opened in a dialog"
        def origin = navigateToView(BlankTestView)
        def outcomes = []
        DialogWindow<OrderDetailTestView> dialog = dialogWindows.detail(origin, Order)
                .withViewClass(OrderDetailTestView)
                .editEntity(orderToEdit)
                .withAfterCloseListener { event -> outcomes << outcomeOf(event) }
                .build()
        dialog.open()
        OrderDetailTestView view = dialog.getView()

        when: "the entity is saved and the view is closed with DISCARD"
        view.getEditedEntity().number = 'saved'
        view.save()
        view.closeWithDiscard()

        then: "only CLOSE is replaced, so DISCARD is kept"
        outcomes == [StandardOutcome.DISCARD]
    }

    private static StandardOutcome outcomeOf(event) {
        StandardOutcome.values().find { event.closedWith(it) }
    }
}

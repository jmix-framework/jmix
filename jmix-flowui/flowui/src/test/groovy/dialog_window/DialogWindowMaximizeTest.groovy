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

package dialog_window

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.dom.Element
import dialog_window.view.CustomHeaderDialogTestView
import dialog_window.view.DialogHostTestView
import dialog_window.view.MaximizableDialogTestView
import dialog_window.view.PlainDialogTestView
import io.jmix.core.Messages
import io.jmix.flowui.DialogWindows
import io.jmix.flowui.component.UiComponentUtils
import io.jmix.flowui.view.DialogWindow
import io.jmix.flowui.view.View
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DialogWindowMaximizeTest extends FlowuiTestSpecification {

    protected static final String MAXIMIZE_BUTTON_CLASS_NAME = "jmix-dialog-window-maximize-button"

    @Autowired
    DialogWindows dialogWindows
    @Autowired
    Messages messages

    @Override
    void setup() {
        registerViewBasePackages("dialog_window.view")
    }

    def "maximize button is hidden until the dialog is made maximizable"() {
        given: "A dialog window of a non-maximizable view"
        def dialogWindow = openDialog(PlainDialogTestView)

        expect: "Its maximize button is present but hidden"
        !dialogWindow.isMaximizable()
        !findMaximizeButton(dialogWindow).visible
    }

    def "maximize button becomes visible when maximizable is set"() {
        given: "An open dialog window of a non-maximizable view"
        def dialogWindow = openDialog(PlainDialogTestView)

        when: "It is made maximizable"
        dialogWindow.setMaximizable(true)

        then: "The maximize button is visible"
        dialogWindow.isMaximizable()
        findMaximizeButton(dialogWindow).visible
    }

    def "maximizing marks the dialog and suppresses dragging and resizing"() {
        given: "An open maximizable dialog window"
        def dialogWindow = openDialog(MaximizableDialogTestView)

        when: "It is maximized"
        dialogWindow.setMaximized(true)

        then: "The dialog element is marked and can neither be dragged nor resized"
        dialogWindow.isMaximized()

        def dialog = UiComponentUtils.findDialog(dialogWindow.view)
        dialog.element.hasAttribute("jmix-maximized")
        !dialog.isResizable()
        !dialog.isDraggable()
    }

    def "restoring removes the mark and brings dragging and resizing back"() {
        given: "A maximized dialog window"
        def dialogWindow = openDialog(MaximizableDialogTestView)
        dialogWindow.setMaximized(true)

        when: "It is restored"
        dialogWindow.setMaximized(false)

        then: "The mark is gone and the previous behaviour is back"
        !dialogWindow.isMaximized()

        def dialog = UiComponentUtils.findDialog(dialogWindow.view)
        !dialog.element.hasAttribute("jmix-maximized")
        dialog.isResizable()
        dialog.isDraggable()
    }

    def "resizable changed while maximized is applied on restore"() {
        given: "A maximized dialog window"
        def dialogWindow = openDialog(MaximizableDialogTestView)
        dialogWindow.setMaximized(true)

        when: "Resizing is turned off while the dialog is maximized"
        dialogWindow.setResizable(false)

        then: "The configured value is reported and the dialog stays non-resizable"
        !dialogWindow.isResizable()
        !UiComponentUtils.findDialog(dialogWindow.view).isResizable()

        when: "The dialog is restored"
        dialogWindow.setMaximized(false)

        then: "The value configured while maximized wins"
        !dialogWindow.isResizable()
        !UiComponentUtils.findDialog(dialogWindow.view).isResizable()
    }

    def "maximizing does not touch the dialog geometry"() {
        given: "An open maximizable dialog window that was resized by the user"
        def dialogWindow = openDialog(MaximizableDialogTestView)

        def dialog = UiComponentUtils.findDialog(dialogWindow.view)
        dialog.setWidth("42em")
        dialog.setHeight("13em")
        dialog.setTop("11px")
        dialog.setLeft("17px")

        when: "It is maximized and restored"
        dialogWindow.setMaximized(true)
        dialogWindow.setMaximized(false)

        then: "Size and position are untouched"
        dialog.width == "42em"
        dialog.height == "13em"
        dialog.top == "11px"
        dialog.left == "17px"
    }

    def "clicking the maximize button toggles the state and notifies the listener"() {
        given: "An open maximizable dialog window with a listener"
        def dialogWindow = openDialog(MaximizableDialogTestView)

        def events = []
        dialogWindow.addMaximizedChangeListener { event -> events.add(event) }

        when: "The maximize button is clicked"
        findMaximizeButton(dialogWindow).click()

        then: "The dialog is maximized and the event reports a client-side change"
        dialogWindow.isMaximized()
        events.size() == 1
        events[0].isMaximized()
        events[0].isFromClient()
        events[0].source.is(dialogWindow)

        when: "The button is clicked again"
        findMaximizeButton(dialogWindow).click()

        then: "The dialog is restored and the second event is fired"
        !dialogWindow.isMaximized()
        events.size() == 2
        !events[1].isMaximized()
    }

    def "programmatic change reports fromClient false"() {
        given: "An open maximizable dialog window with a listener"
        def dialogWindow = openDialog(MaximizableDialogTestView)

        def events = []
        dialogWindow.addMaximizedChangeListener { event -> events.add(event) }

        when: "It is maximized programmatically"
        dialogWindow.setMaximized(true)

        then: "The event is not marked as client-side"
        events.size() == 1
        !events[0].isFromClient()
    }

    def "maximized change event exposes the dialog window and its view"() {
        given: "An open maximizable dialog window with a listener"
        def dialogWindow = openDialog(MaximizableDialogTestView)

        def events = []
        dialogWindow.addMaximizedChangeListener { event -> events.add(event) }

        when: "It is maximized"
        dialogWindow.setMaximized(true)

        then: "The event points to the dialog window and to the view shown in it"
        events.size() == 1
        events[0].getSource().is(dialogWindow)
        events[0].getView().is(dialogWindow.view)
        events[0].getView() instanceof MaximizableDialogTestView
    }

    def "listener is not notified when the state does not change"() {
        given: "An open maximizable dialog window with a listener"
        def dialogWindow = openDialog(MaximizableDialogTestView)

        def events = []
        dialogWindow.addMaximizedChangeListener { event -> events.add(event) }

        when: "The current state is set again"
        dialogWindow.setMaximized(false)

        then: "No event is fired"
        events.isEmpty()
    }

    def "maximizable is taken from the DialogMode annotation"() {
        when: "A view annotated as maximizable is opened in a dialog"
        def dialogWindow = openDialog(MaximizableDialogTestView)

        then: "The maximize button is visible without any extra call"
        dialogWindow.isMaximizable()
        findMaximizeButton(dialogWindow).visible
    }

    def "maximize button icon and description reflect the state"() {
        given: "An open maximizable dialog window"
        def dialogWindow = openDialog(MaximizableDialogTestView)
        def button = findMaximizeButton(dialogWindow)

        expect: "The button offers to maximize the dialog"
        button.element.getProperty("title") == messages.getMessage("dialogWindow.maximizeButton.description")
        button.icon.iconClassNames.any { it == "jmix-font-icon-expand" }

        when: "The dialog is maximized"
        dialogWindow.setMaximized(true)

        then: "The button offers to restore the dialog"
        button.element.getProperty("title") == messages.getMessage("dialogWindow.restoreButton.description")
        button.icon.iconClassNames.any { it == "jmix-font-icon-compress" }
    }

    def "components added via configureDialogWindowHeader stay in the header before the buttons"() {
        when: "A view that contributes a header component is opened as a dialog"
        def dialogWindow = openDialog(CustomHeaderDialogTestView)

        then: "The contributed component is in the header content"
        def dialog = UiComponentUtils.findDialog(dialogWindow.view)
        def headerWrapper = findByClassName(dialog.header.element, "jmix-dialog-window-header-wrapper").get()
        def headerContent = findByClassName(headerWrapper, "jmix-dialog-window-header-content").get()

        headerContent.children
                .anyMatch { Element child -> child.getAttribute("id") == CustomHeaderDialogTestView.CUSTOM_HEADER_BUTTON_ID }

        and: "The header keeps the order: contributed content, maximize button, close button"
        def wrapperClasses = headerWrapper.children
                .map { Element child -> child.classList.toList() }
                .toList()

        wrapperClasses.size() == 3
        wrapperClasses[0].contains("jmix-dialog-window-header-content")
        wrapperClasses[1].contains("jmix-dialog-window-maximize-button")
        wrapperClasses[2].contains("jmix-dialog-window-close-button")

        and: "Both header buttons are visible"
        findMaximizeButton(dialogWindow).visible
    }

    protected <V extends View<?>> DialogWindow<V> openDialog(Class<V> viewClass) {
        def origin = navigateToView(DialogHostTestView)
        def dialogWindow = dialogWindows.view(origin, viewClass).build()
        dialogWindow.open()
        return dialogWindow
    }

    protected Button findMaximizeButton(DialogWindow<?> dialogWindow) {
        def dialog = UiComponentUtils.findDialog(dialogWindow.view)
        def button = findByClassName(dialog.header.element, MAXIMIZE_BUTTON_CLASS_NAME)
                .flatMap { Element element -> element.component }
                .orElse(null)

        assert button instanceof Button
        return (Button) button
    }

    protected Optional<Element> findByClassName(Element root, String className) {
        if (root.classList.contains(className)) {
            return Optional.of(root)
        }

        return root.children
                .map { Element child -> findByClassName(child, className) }
                .filter { Optional<Element> found -> found.isPresent() }
                .findFirst()
                .orElse(Optional.empty())
    }
}

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

package component.menubar

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.contextmenu.MenuItem
import component.menubar.test_support.TestMenuBar
import component.menubar.view.MenuBarTestView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class JmixMenuBarTest extends FlowuiTestSpecification {

    void setup() {
        registerViewBasePackages("component.menubar.view")
    }

    def "Changing the class name of a root item re-renders the buttons without a client-side call"() {
        given: "A root item of a menu bar attached to a view"
        def menuBar = new TestMenuBar()
        def rootItem = attachWithRootItem(menuBar)

        when: "A class name is added to the root item"
        rootItem.addClassName("customClassName")

        then: "The buttons are re-rendered and no removed connector function is called"
        menuBar.updateButtonsCalls > 0
        flushPendingJavaScript().every { !it.contains('menubarConnector.setClassName') }
    }

    def "Changing the tooltip of a root item re-renders the buttons"() {
        given: "A root item of a menu bar attached to a view"
        def menuBar = new TestMenuBar()
        def rootItem = attachWithRootItem(menuBar)

        when: "A tooltip is set to the root item"
        rootItem.setTooltipText("Tooltip")

        then: "The buttons are re-rendered"
        menuBar.updateButtonsCalls > 0
    }

    protected MenuItem attachWithRootItem(TestMenuBar menuBar) {
        def view = navigateToView(MenuBarTestView)
        def rootItem = menuBar.addItem("Item")
        view.content.add(menuBar)

        flushPendingJavaScript()
        menuBar.resetUpdateButtonsCalls()
        return rootItem
    }

    protected List<String> flushPendingJavaScript() {
        def internals = UI.current.internals
        internals.stateTree.runExecutionsBeforeClientResponse()
        return internals.dumpPendingJavaScriptInvocations()*.invocation*.expression
    }
}

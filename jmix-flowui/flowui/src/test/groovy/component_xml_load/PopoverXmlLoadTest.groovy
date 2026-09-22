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

package component_xml_load

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.popover.PopoverPosition
import component_xml_load.screen.BrokenPopoverTargetView
import component_xml_load.screen.PopoverFragmentView
import component_xml_load.screen.PopoverTestView
import io.jmix.flowui.exception.GuiDevelopmentException
import io.jmix.flowui.fragment.FragmentUtils
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class PopoverXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load Popover attributes"() {
        when: "Open the view"
        def view = navigateToView(PopoverTestView)

        then: "Check all attributes"
        verifyAll(view.popover) {
            id.get() == "popover"
            ariaLabel.orElse(null) == "ariaLabel"
            ariaLabelledBy.orElse(null) == "ariaLabelledBy"
            autofocus
            backdropVisible
            classNames.containsAll(["className1", "className2"])
            !closeOnEsc
            !closeOnOutsideClick
            !enabled
            focusDelay == 100
            hideDelay == 300
            hoverDelay == 200
            modal
            !openOnClick
            openOnFocus
            openOnHover
            position == PopoverPosition.BOTTOM_END
            role == "tooltip"
            !tabFocusEnabled
            themeNames.containsAll(["arrow", "no-padding"])
            !visible
        }

        and: "Size is written as element properties, not styles"
        view.popover.element.getProperty("width") == "300px"
        view.popover.element.getProperty("height") == "200px"
    }

    def "Popover content is loaded and reachable by id"() {
        when: "Open the view"
        def view = navigateToView(PopoverTestView)

        then: "Every child is inside the popover and injected into the controller"
        view.popoverContentBox != null
        view.popoverContentButton != null

        and: "A popover holds an unbounded, mixed list of children, not a single slot"
        def children = view.popover.children.toList()
        children == [view.popoverContentBox, view.popoverContentButton]
    }

    def "Popover without attributes keeps Vaadin defaults"() {
        when: "Open the view"
        def view = navigateToView(PopoverTestView)

        then: "Defaults are untouched"
        verifyAll(view.plainPopover) {
            // getPosition() reads the 'position' element property, which the loader leaves unset;
            // the BOTTOM_START default lives in the web component, not on the server side.
            position == null
            openOnClick
            !openOnHover
            !openOnFocus
            closeOnEsc
            closeOnOutsideClick
            !modal
            visible
        }
    }

    def "Popover target is resolved by id"() {
        when: "Open the view"
        def view = navigateToView(PopoverTestView)

        then: "The target is the component with that id"
        view.popover.target === view.getContent().getChildren()
                .filter { Component c -> c.id.orElse(null) == "targetButton" }
                .findFirst().orElse(null)
    }

    def "Popover without target stays unbound"() {
        when: "Open the view"
        def view = navigateToView(PopoverTestView)

        then: "No target is set"
        view.plainPopover.target == null
    }

    def "Unknown target id is a development error"() {
        when: "Open a view whose popover points at a missing component"
        navigateToView(BrokenPopoverTargetView)

        then: "The loader reports a development error"
        def exception = thrown(GuiDevelopmentException)
        exception.message.contains("noSuchComponent")
    }

    def "Popover inside a fragment resolves a target of that fragment"() {
        when: "Open the view hosting the fragment"
        def view = navigateToView(PopoverFragmentView)

        then: "The target is bound"
        def target = view.popoverFragment.fragmentPopover.target
        target != null
        FragmentUtils.getComponentId(target).orElse(null) == "fragmentTargetButton"
    }
}

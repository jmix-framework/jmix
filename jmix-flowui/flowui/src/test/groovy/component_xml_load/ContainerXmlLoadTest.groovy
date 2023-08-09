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

package component_xml_load

import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.accordion.AccordionPanel
import com.vaadin.flow.component.orderedlayout.BoxSizing
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.orderedlayout.Scroller
import com.vaadin.flow.component.shared.Tooltip
import com.vaadin.flow.component.tabs.Tab
import com.vaadin.flow.component.tabs.TabSheetVariant
import com.vaadin.flow.component.tabs.Tabs
import component_xml_load.screen.ContainerView
import io.jmix.flowui.component.textfield.TypedTextField
import io.jmix.flowui.kit.component.button.JmixButton
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ContainerXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    @SuppressWarnings('GrUnresolvedAccess')
    def "Load #container container from XML"() {
        when: "Open the ContainerView"
        def containerView = navigateToView(ContainerView.class)

        then: "#container attributes will be loaded"
        verifyAll(containerView."${container}Id") {
            id.get() == "${container}Id"
            alignItems == FlexComponent.Alignment.STRETCH
            boxSizing == BoxSizing.BORDER_BOX
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            height == "50px"
            justifyContentMode == FlexComponent.JustifyContentMode.AROUND
            margin
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            padding
            spacing
            width == "100px"
            (getChildren().find { it instanceof TypedTextField<?> } as TypedTextField<?>).id.get() == "expanded"
            (getChildren().find { it instanceof JmixButton } as JmixButton).text == "${container}Child"
        }

        where:
        container << ["vbox", "hbox"]
    }

    def "Load accordion container from XML"() {
        when: "Open the ContainerView"
        def containerView = navigateToView(ContainerView.class)

        then: "Accordion attributes will be loaded"
        verifyAll(containerView.accordionId) {
            id.get() == "accordionId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            visible
            width == "100px"
        }
    }

    def "Load #accordionPanel container from XML"() {
        when: "Open the ContainerView"
        def containerView = navigateToView(ContainerView.class)

        then: "AccordionPanel attributes will be loaded"
        def panel = containerView.accordionId.children.find { it.id.get() == "${accordionPanel}Id" }
        verifyAll(panel as AccordionPanel) {
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            height == "50px"
            id.get() == "${accordionPanel}Id"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            summaryText == "summaryTextString"
            themeNames.containsAll(["small", "reverse"])
            visible
            width == "100px"
        }

        where:
        accordionPanel << ["accordionPanel", "anotherAccordionPanel"]
    }

    def "Load accordionPanel tooltip from XML"() {
        when: "Open the ContainerView"
        def containerView = navigateToView(ContainerView.class)

        then: "AccordionPanel tooltip will be loaded"
        def panel = containerView.accordionId.children.find {it.id.get() == "accordionPanelId"}
        verifyAll (panel as AccordionPanel) {
            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load scroller container from XML"() {
        when: "Open the ContainerView"
        def containerView = navigateToView(ContainerView.class)

        then: "Scroller attributes will be loaded"
        verifyAll(containerView.scrollerId) {
            id.get() == "scrollerId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            scrollDirection == Scroller.ScrollDirection.BOTH
            visible
            width == "100px"
            getOwnComponents().first().id.get() == "scrollerChild"
        }
    }

    def "Load tabs container from XML"() {
        when: "Open the ContainerView"
        def containerView = navigateToView(ContainerView.class)

        then: "Tabs attributes will be loaded"
        def tabs = containerView.tabsId

        verifyAll(tabs) {
            id.get() == "tabsId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            orientation == Tabs.Orientation.HORIZONTAL
            themeNames.containsAll(["small", "minimal"])
            visible
            width == "100px"
        }

        def tabsChild = tabs.children.toArray()

        verifyAll(tabsChild[0] as Tab) {
            id.get() == "tab1"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            flexGrow == 45.54d
            label == "labelString"
            themeName == "icon-on-top"
            visible
            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }

        verifyAll(tabsChild[1] as Tab) {
            id.get() == "tab2"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            flexGrow == 45.44d
            themeName == "icon-on-top"
            visible
            (children.findAny().get() as TypedTextField<?>).id.get() == "tab2Child"
        }
    }


    def "Load tabSheet component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ContainerView.class)

        then: "TabSheet attributes will be loaded"
        def tabSheet = componentView.tabSheetId
        def tabs = tabSheet.getChildren().collect()
        def childHbox = tabSheet.getOwnComponents()[0] as HorizontalLayout

        verifyAll(tabSheet) {
            id.get() == "tabSheetId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            themeNames.containsAll([TabSheetVariant.LUMO_TABS_SMALL.getVariantName(),
                                    TabSheetVariant.LUMO_BORDERED.getVariantName()])
            visible
            width == "100px"

            (tabs[0] as Tab).getId().orElse(null) == "tab1"
            (tabs[0] as Tab).getLabel() == "tab1Label"
            (childHbox.getComponentAt(0) as HasText).text == "tab1Child1"
            (childHbox.getComponentAt(1) as HasText).text == "tab1Child2"

            (tabs[0] as Tab).tooltip.text == "tooltipText"
            (tabs[0] as Tab).tooltip.focusDelay == 1
            (tabs[0] as Tab).tooltip.hideDelay == 2
            (tabs[0] as Tab).tooltip.hoverDelay == 3
            (tabs[0] as Tab).tooltip.manual
            (tabs[0] as Tab).tooltip.opened
            (tabs[0] as Tab).tooltip.position == Tooltip.TooltipPosition.BOTTOM

            (tabs[1] as Tab).getId().orElse(null) == "tab2"
            (tabs[1] as Tab).getLabel() == "tab2Label"
            (tabSheet.getContentByTab(tabs[1] as Tab) as TypedTextField).getValue() == "tab2Child"
        }
    }
}

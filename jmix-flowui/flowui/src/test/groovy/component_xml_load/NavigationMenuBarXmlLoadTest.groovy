/*
 * Copyright 2023 Haulmont.
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

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.KeyModifier
import com.vaadin.flow.component.icon.VaadinIcon
import component_xml_load.screen.NavigationMenuBarView
import io.jmix.flowui.component.navigationmenubar.NavigationMenuBar
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(["jmix.ui.composite-menu=false", "jmix.ui.menu-config=menu/navigationmenubar/menu.xml"])
class NavigationMenuBarXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load NavigationMenuBar component from XML"() {
        when: "Open the NavigationMenuBarView"
        def navigationMenuBarView = navigateToView(NavigationMenuBarView.class)

        then: "NavigationMenuBar attributes will be loaded"

        def menu = navigationMenuBarView.navigationMenuBar
        verifyAll(menu) {
            id.get() == "navigationMenuBar"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            height == "15em"
            maxHeight == "20em"
            maxWidth == "40em"
            minHeight == "10em"
            minWidth == "20em"
            visible
            width == "30em"

            getMenuItems().size() == 2
        }

        def applicationItem = menu.getMenuItem("application")
        applicationItem != null
        applicationItem instanceof NavigationMenuBar.ParentMenuItem

        def parentApplicationItem = (NavigationMenuBar.ParentMenuItem) applicationItem

        parentApplicationItem.getId().isPresent()
        parentApplicationItem.getId().get() == "application"
        parentApplicationItem.getTitle() == "Application title"
        parentApplicationItem.getIcon().element.getAttribute("icon") ==
                VaadinIcon.TABLE.create().element.getAttribute("icon")
        parentApplicationItem.getClassNames().containsAll(List.of("className1", "className2"))
        parentApplicationItem.getChildItems().size() == 3
        parentApplicationItem.getMenu() == menu
        parentApplicationItem.getParentMenuItem() == null
        parentApplicationItem.getTooltip().getText() == "Application"

        def applicationViewItem = menu.getMenuItem("Application.view")
        applicationViewItem != null
        applicationViewItem instanceof NavigationMenuBar.ViewMenuItem

        def applicationViewMenuItem = (NavigationMenuBar.ViewMenuItem) applicationViewItem

        applicationViewMenuItem.getId().isPresent()
        applicationViewMenuItem.getId().get() == "Application.view"
        applicationViewMenuItem.getTitle() == "Application view"
        applicationViewMenuItem.getIcon().element.getAttribute("icon") ==
                VaadinIcon.ABACUS.create().element.getAttribute("icon")
        applicationViewMenuItem.getMenu() == menu
        applicationViewMenuItem.getParentMenuItem() == applicationItem
        applicationViewMenuItem.getTooltip().getText() == "app view"
        applicationViewMenuItem.getShortcutCombination() != null
        applicationViewMenuItem.getShortcutCombination().getKey() == Key.ENTER
        Arrays.equals(applicationViewMenuItem.getShortcutCombination().getKeyModifiers(), new KeyModifier[]{KeyModifier.CONTROL})
        applicationViewMenuItem.getViewClass() == NavigationMenuBarView.class
        applicationViewMenuItem.getUrlQueryParameters().getParameters().isEmpty()
        applicationViewMenuItem.getRouteParameters().getParameterNames().isEmpty()

        def separatorItem = parentApplicationItem.getChildItems().get(1)
        separatorItem instanceof NavigationMenuBar.SeparatorMenuItem
        separatorItem.getParentMenuItem() == parentApplicationItem
        separatorItem.getMenu() == menu
        separatorItem.getTitle() == null

        def nestedMenuItem = menu.getMenuItem("nestedMenu")
        nestedMenuItem != null
        nestedMenuItem instanceof NavigationMenuBar.ParentMenuItem

        def parentNestedMenuItem = (NavigationMenuBar.ParentMenuItem) nestedMenuItem

        parentNestedMenuItem.getId().isPresent()
        parentNestedMenuItem.getId().get() == "nestedMenu"
        parentNestedMenuItem.getTitle() == "Nested menu"
        parentNestedMenuItem.getIcon() == null
        parentNestedMenuItem.getChildItems().size() == 1
        parentNestedMenuItem.getMenu() == menu
        parentNestedMenuItem.getParentMenuItem() == applicationItem
        parentNestedMenuItem.getTooltip().getText() == null

        def nestedViewItem = menu.getMenuItem("Nested.view")
        nestedViewItem != null
        nestedViewItem instanceof NavigationMenuBar.ViewMenuItem

        def nestedViewMenuItem = (NavigationMenuBar.ViewMenuItem) nestedViewItem

        nestedViewMenuItem.getId().isPresent()
        nestedViewMenuItem.getId().get() == "Nested.view"
        nestedViewMenuItem.getTitle() == "Nested view"
        nestedViewMenuItem.getIcon() == null
        nestedViewMenuItem.getMenu() == menu
        nestedViewMenuItem.getParentMenuItem() == nestedMenuItem
        nestedViewMenuItem.getTooltip().getText() == null
        nestedViewMenuItem.getShortcutCombination() == null
        nestedViewMenuItem.getViewClass() == NavigationMenuBarView.class
        nestedViewMenuItem.getUrlQueryParameters().getParameters().isEmpty()
        nestedViewMenuItem.getRouteParameters().getParameterNames().isEmpty()

        def administrationViewItem = menu.getMenuItem("Administration.view")
        administrationViewItem != null
        administrationViewItem instanceof NavigationMenuBar.ViewMenuItem

        def administrationViewMenuItem = (NavigationMenuBar.ViewMenuItem) administrationViewItem

        administrationViewMenuItem.getId().isPresent()
        administrationViewMenuItem.getId().get() == "Administration.view"
        administrationViewMenuItem.getTitle() == "Administration view"
        administrationViewMenuItem.getIcon() == null
        administrationViewMenuItem.getMenu() == menu
        administrationViewMenuItem.getParentMenuItem() == null
        administrationViewMenuItem.getTooltip().getText() == null
        administrationViewMenuItem.getShortcutCombination() == null
        administrationViewMenuItem.getViewClass() == NavigationMenuBarView.class
        administrationViewMenuItem.getUrlQueryParameters().getSingleParameter("a").isPresent()
        administrationViewMenuItem.getUrlQueryParameters().getSingleParameter("a").get() == "A"
        administrationViewMenuItem.getRouteParameters().getParameterNames().isEmpty()
    }

    def "Load NavigationMenuBar component without loading items from XML"() {
        when: "Open the NavigationMenuBarView"
        def navigationMenuBarView = navigateToView(NavigationMenuBarView.class)

        then: "NavigationMenuBar items will not be loaded"

        def menu = navigationMenuBarView.navigationMenuBarNoItemsLoad
        verifyAll(menu) {
            id.get() == "navigationMenuBarNoItemsLoad"

            getMenuItems().size() == 0
        }
    }
}

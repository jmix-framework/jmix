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

package component.listmenu

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.KeyModifier
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.dom.Element
import com.vaadin.flow.router.RouterLink
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.main.JmixListMenu
import io.jmix.flowui.kit.component.main.ListMenu
import io.jmix.flowui.menu.ListMenuBuilder
import component.listmenu.test_support.ListMenuTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import component.listmenu.test_support.TestMenuConfig
import test_support.ComponentTestUtils
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(classes = [ListMenuTestConfiguration])
class ListMenuTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    @Autowired
    TestMenuConfig menuConfig

    @Autowired
    ListMenuBuilder listMenuBuilder

    @Override
    void setup() {
        registerViewBasePackages("component.listmenu.view")
    }

    def "Load menu form XML"() {
        when:
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <menu id="application"
                      title="Application"
                      opened="true"
                      icon="vaadin:abacus"
                      description="Description">
                    <item view="ListMenuTestView"
                          title="List menu test view"
                          icon="vaadin:academy-cap"
                          description="Description"
                          shortcutCombination="O"/>
                    <item bean="TestMenuItemBean" 
                          beanMethod="open"
                          title="Bean menu"
                          icon="vaadin:abacus"
                          description="Description"
                          shortcutCombination="Control-O"/>
                </menu>
           </menu-config>
        '''.trim())

        def listMenu = uiComponents.create(JmixListMenu)

        listMenuBuilder.build(listMenu)

        then:

        listMenu.menuItems.size() == 1

        def applicationMenuBar = (ListMenu.MenuBarItem) listMenu.getMenuItem("application")
        applicationMenuBar != null
        applicationMenuBar.title == "Application"
        applicationMenuBar.opened
        applicationMenuBar.description == "Description"
        ComponentTestUtils.isSameIcon(applicationMenuBar.prefixComponent, VaadinIcon.ABACUS)
        applicationMenuBar.getChildItems().size() == 2

        def menuItem = listMenu.getMenuItem("ListMenuTestView")
        menuItem != null
        menuItem.title == "List menu test view"
        ComponentTestUtils.isSameIcon(menuItem.prefixComponent, VaadinIcon.ACADEMY_CAP)
        menuItem.description == "Description"
        menuItem.shortcutCombination.key.keys.get(0) == "KeyO"

        def menuBeanItem = (JmixListMenu.BeanMenuItem) listMenu.getMenuItem("TestMenuItemBean#open")
        menuBeanItem != null
        menuBeanItem.title == "Bean menu"
        ComponentTestUtils.isSameIcon(menuBeanItem.prefixComponent, VaadinIcon.ABACUS)
        menuBeanItem.description == "Description"
        menuBeanItem.shortcutCombination.key.keys.get(0) == "KeyO"
        menuBeanItem.shortcutCombination.keyModifiers[0] == KeyModifier.CONTROL
    }

    def "Render menu item components according to item type"() {
        when:
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <item view="ListMenuTestView"
                      title="View menu"/>
                <item bean="TestMenuItemBean"
                      beanMethod="open"
                      title="Bean menu"/>
           </menu-config>
        '''.trim())

        def listMenu = uiComponents.create(JmixListMenu)

        listMenuBuilder.build(listMenu)

        then:
        def viewMenuItemComponent = getRenderedMenuItemComponent(listMenu, "ListMenuTestView")
        viewMenuItemComponent instanceof RouterLink
        viewMenuItemComponent.element.tag == "a"
        viewMenuItemComponent.classNames.containsAll([
                "jmix-menu-item",
                "jmix-menu-item-view"
        ])

        def beanMenuItemComponent = getRenderedMenuItemComponent(listMenu, "TestMenuItemBean#open")
        beanMenuItemComponent instanceof Button
        beanMenuItemComponent.element.tag == "vaadin-button"
        beanMenuItemComponent.text == "Bean menu"
        !beanMenuItemComponent.element.hasAttribute("href")
        !hasChildWithClassName(beanMenuItemComponent, "link-text")
        beanMenuItemComponent.classNames.containsAll([
                "jmix-menu-item",
                "jmix-menu-item-bean"
        ])
    }

    def "Apply prefix and suffix components to rendered menu item components"() {
        when:
        def listMenu = uiComponents.create(JmixListMenu)

        def viewPrefixComponent = new Icon(VaadinIcon.ACADEMY_CAP)
        def viewSuffixComponent = new Span("view")
        def beanPrefixComponent = new Icon(VaadinIcon.ABACUS)
        def beanSuffixComponent = new Span("bean")

        listMenu.addMenuItem(JmixListMenu.ViewMenuItem.create("ListMenuTestView")
                .withPrefixComponent(viewPrefixComponent)
                .withSuffixComponent(viewSuffixComponent))
        listMenu.addMenuItem(JmixListMenu.BeanMenuItem.create("beanMenuItem")
                .withPrefixComponent(beanPrefixComponent)
                .withSuffixComponent(beanSuffixComponent))

        then:
        def viewMenuItemComponent = getRenderedMenuItemComponent(listMenu, "ListMenuTestView")
        isChild(viewMenuItemComponent, viewPrefixComponent)
        isChild(viewMenuItemComponent, viewSuffixComponent)
        viewPrefixComponent.classNames.contains("prefix-component")
        viewSuffixComponent.classNames.contains("suffix-component")

        def beanMenuItemComponent = getRenderedMenuItemComponent(listMenu, "beanMenuItem")
        isChild(beanMenuItemComponent, beanPrefixComponent)
        isChild(beanMenuItemComponent, beanSuffixComponent)
        beanPrefixComponent.classNames.contains("prefix-component")
        beanPrefixComponent.element.getAttribute("slot") == "prefix"
        beanSuffixComponent.classNames.contains("suffix-component")
        beanSuffixComponent.element.getAttribute("slot") == "suffix"
    }

    def "Set and add className to menu item"() {
        when: "Load menu items with class names"
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <menu id="application"
                      classNames="class1">
                    <item view="ListMenuTestView"
                          classNames="class1"/>
                    <item bean="TestMenuItemBean"
                          beanMethod="open"
                          classNames="class1"/>
                </menu>
           </menu-config>
        '''.trim())

        def listMenu = uiComponents.create(JmixListMenu)

        listMenuBuilder.build(listMenu)

        then: "Class names should be loaded and we still able adding/setting class names."

        def className1 = "class1"
        def className2 = "class2"
        def className3 = "class3"

        def menuItems = [listMenu.getMenuItem("application"),
                         listMenu.getMenuItem("ListMenuTestView"), listMenu.getMenuItem("TestMenuItemBean#open")]

        for (ListMenu.MenuItem item : menuItems) {
            assert item.classNames.contains(className1)

            item.addClassNames(className2)
            assert item.classNames.contains(className2)

            item.withClassNames([className3])
            assert !item.classNames.contains(className1)
            assert !item.classNames.contains(className2)
        }

        def applicationComponent = getRenderedMenuItemComponent(listMenu, "application")
        applicationComponent.classNames.contains("jmix-menubar-item")
        applicationComponent.classNames.contains(className3)
        !applicationComponent.classNames.contains(className1)
        !applicationComponent.classNames.contains(className2)

        def viewMenuItemComponent = getRenderedMenuItemComponent(listMenu, "ListMenuTestView")
        viewMenuItemComponent.classNames.containsAll([
                "jmix-menu-item",
                "jmix-menu-item-view",
                className3
        ])
        !viewMenuItemComponent.classNames.contains(className1)
        !viewMenuItemComponent.classNames.contains(className2)

        def beanMenuItemComponent = getRenderedMenuItemComponent(listMenu, "TestMenuItemBean#open")
        beanMenuItemComponent.classNames.containsAll([
                "jmix-menu-item",
                "jmix-menu-item-bean",
                className3
        ])
        !beanMenuItemComponent.classNames.contains(className1)
        !beanMenuItemComponent.classNames.contains(className2)
    }

    protected static Component getRenderedMenuItemComponent(JmixListMenu listMenu, String itemId) {
        Element menuItemElement = findElementById(listMenu.element, itemId)
        assert menuItemElement != null

        Element componentElement = menuItemElement.children.findFirst()
                .orElseThrow(() -> new IllegalStateException("Menu item component not found: " + itemId))

        return componentElement.component
                .orElseThrow(() -> new IllegalStateException("Component is not attached to element: " + itemId))
    }

    protected static Element findElementById(Element element, String id) {
        if (id == element.getAttribute("id")) {
            return element
        }

        Iterator<Element> iterator = element.children.iterator()
        while (iterator.hasNext()) {
            Element child = iterator.next()
            Element result = findElementById(child, id)
            if (result != null) {
                return result
            }
        }

        return null
    }

    protected static boolean isChild(Component component, Component child) {
        return component.element.children.anyMatch { element -> element == child.element }
    }

    protected static boolean hasChildWithClassName(Component component, String className) {
        return component.element.children.anyMatch { element ->
            String classNames = element.getAttribute("class")
            return classNames != null && classNames.split(" ").contains(className)
        }
    }
}

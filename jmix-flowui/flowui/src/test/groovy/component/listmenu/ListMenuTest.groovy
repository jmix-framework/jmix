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

import com.vaadin.flow.component.KeyModifier
import com.vaadin.flow.component.icon.VaadinIcon
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.main.JmixListMenu
import io.jmix.flowui.kit.component.main.ListMenu
import io.jmix.flowui.menu.ListMenuBuilder
import component.listmenu.test_support.ListMenuTestConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import component.listmenu.test_support.TestMenuConfig
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
                      icon="ABACUS"
                      description="Description">
                    <item view="ListMenuTestView"
                          title="List menu test view"
                          icon="ABACUS"
                          description="Description"
                          shortcutCombination="O"/>
                    <item bean="TestMenuItemBean" 
                          beanMethod="open"
                          title="Bean menu"
                          icon="ABACUS"
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
        applicationMenuBar.icon == VaadinIcon.ABACUS
        applicationMenuBar.description == "Description"
        applicationMenuBar.getChildItems().size() == 2

        def menuItem = listMenu.getMenuItem("ListMenuTestView")
        menuItem != null
        menuItem.title == "List menu test view"
        menuItem.icon == VaadinIcon.ABACUS
        menuItem.description == "Description"
        menuItem.shortcutCombination.key.keys.get(0) == "KeyO"

        def menuBeanItem = (JmixListMenu.BeanMenuItem) listMenu.getMenuItem("TestMenuItemBean#open")
        menuBeanItem != null
        menuBeanItem.title == "Bean menu"
        menuBeanItem.icon == VaadinIcon.ABACUS
        menuBeanItem.description == "Description"
        menuBeanItem.shortcutCombination.key.keys.get(0) == "KeyO"
        menuBeanItem.shortcutCombination.keyModifiers[0] == KeyModifier.CONTROL
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
    }
}

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

package component.horizontalmenu

import component.listmenu.test_support.ListMenuTestConfiguration
import component.listmenu.test_support.TestMenuConfig
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.horizontalmenu.HorizontalMenu
import io.jmix.flowui.component.horizontalmenu.MenuConfigHorizontalMenuItemProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(classes = [ListMenuTestConfiguration])
class HorizontalMenuItemConverterTest extends FlowuiTestSpecification {

    @Autowired
    UiComponents uiComponents

    @Autowired
    TestMenuConfig menuConfig

    @Override
    void setup() {
        registerViewBasePackages("component.listmenu.view")
    }

    def "Create items and menus with visible=false hidden: the ticket's example"() {
        given:
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <menu id="admin" title="Admin">
                    <item id="users" view="ListMenuTestView"/>
                    <item id="hiddenUsers" view="ListMenuTestView" title="Hidden" visible="false"/>
                </menu>
                <menu id="hiddenMenu" title="Hidden menu" visible="false">
                    <item id="hiddenMenuUsers" view="ListMenuTestView"/>
                </menu>
           </menu-config>
        '''.trim())

        when:
        def menu = loadMenu()

        then:
        def admin = menu.getMenuItem("admin")
        admin.visible
        admin.menuItemWrapper.visible

        def users = menu.getMenuItem("users")
        users.visible
        users.menuItemWrapper.visible

        def hiddenUsers = menu.getMenuItem("hiddenUsers")
        hiddenUsers != null
        !hiddenUsers.visible
        !hiddenUsers.menuItemWrapper.visible

        def hiddenMenu = menu.getMenuItem("hiddenMenu")
        hiddenMenu != null
        !hiddenMenu.visible
        !hiddenMenu.menuItemWrapper.visible
    }

    def "Create a bean item with visible=false hidden"() {
        given:
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <menu id="admin" title="Admin">
                    <item id="users" view="ListMenuTestView"/>
                    <item id="hiddenBean" bean="TestMenuItemBean" beanMethod="open" visible="false"/>
                </menu>
           </menu-config>
        '''.trim())

        when:
        def menu = loadMenu()

        then:
        def hiddenBean = menu.getMenuItem("hiddenBean")
        hiddenBean != null
        !hiddenBean.visible
        !hiddenBean.menuItemWrapper.visible
    }

    def "Hide separators left after hidden items"() {
        given:
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <menu id="admin" title="Admin">
                    <item id="users" view="ListMenuTestView" visible="false"/>
                    <separator/>
                    <item id="roles" view="ListMenuTestView" visible="false"/>
                    <item id="sessions" view="ListMenuTestView" visible="false"/>
                    <separator/>
                    <item id="audit" view="ListMenuTestView"/>
                </menu>
           </menu-config>
        '''.trim())

        when:
        def menu = loadMenu()

        then:
        def admin = (HorizontalMenu.ParentMenuItem) menu.getMenuItem("admin")
        admin.childItems.count { it.separator } == 2
        admin.childItems.findAll { it.separator && it.element.visible }.isEmpty()
    }

    def "Skip a submenu whose regular items are all hidden even if it has separators"() {
        given:
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <menu id="admin" title="Admin">
                    <item id="users" view="ListMenuTestView" visible="false"/>
                    <separator/>
                    <item id="roles" view="ListMenuTestView" visible="false"/>
                </menu>
           </menu-config>
        '''.trim())

        when:
        def menu = loadMenu()

        then:
        menu.getMenuItem("admin") == null
        menu.menuItems.isEmpty()
    }

    def "Skip a submenu whose only child is a hidden submenu"() {
        given:
        menuConfig.loadTestMenu('''
            <menu-config xmlns="http://jmix.io/schema/flowui/menu">
                <menu id="outer" title="Outer">
                    <menu id="inner" title="Inner" visible="false">
                        <item id="users" view="ListMenuTestView"/>
                    </menu>
                </menu>
           </menu-config>
        '''.trim())

        when:
        def menu = loadMenu()

        then:
        menu.getMenuItem("outer") == null
        menu.menuItems.isEmpty()
    }

    protected HorizontalMenu loadMenu() {
        def menu = uiComponents.create(HorizontalMenu)
        def provider = applicationContext.getBean(MenuConfigHorizontalMenuItemProvider)
        menu.setMenuItemProvider(provider)
        provider.load()
        return menu
    }
}

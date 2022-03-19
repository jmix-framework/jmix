/*
 * Copyright (c) 2020 Haulmont.
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

package component.menu

import io.jmix.core.CoreConfiguration
import io.jmix.data.DataConfiguration
import io.jmix.eclipselink.EclipselinkConfiguration
import io.jmix.ui.UiConfiguration
import io.jmix.ui.component.mainwindow.SideMenu
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.bean.TestMenuConfig
import test_support.bean.TestSideMenuBuilder
import test_support.UiTestConfiguration

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class SideMenuBuilderTest extends ScreenSpecification {

    @Autowired
    TestMenuConfig menuConfig
    @Autowired
    TestSideMenuBuilder builder

    def "sidemenu builder removes extra empty submenus if they are present in MenuConfig"() {
        given: "menu loads XML with extra empty submenu"

        def mainScreen = showTestMainScreen()
        def menu = uiComponents.create(SideMenu)
        menu.frame = mainScreen.window

        menuConfig.loadTestMenu('''
<menu-config xmlns="http://jmix.io/schema/ui/menu">
    <menu id="MAIN">
        <menu id="A"
              description="F">
            <item id="A1"
                  screen="test_Order.browse"/>
        </menu>

        <menu id="X">

        </menu>

        <menu id="B">
            <item id="B1"
                  screen="test_Order.browse"/>
        </menu>
    </menu>
</menu-config>
'''.trim())

        when: "we build UI menu structure"
        builder.build(menu, menuConfig.rootItems)

        then: "menu should not contain extra empty submenu"
        menu.menuItems.size() == 1
        menu.menuItems[0].children.size() == 2
    }

    def "sidemenu builder does not create separator elements"() {
        given: "menu loads XML with extra empty submenu"

        def mainScreen = showTestMainScreen()
        def menu = uiComponents.create(SideMenu)
        menu.frame = mainScreen.window

        menuConfig.loadTestMenu('''
<menu-config xmlns="http://jmix.io/schema/ui/menu">
    <menu id="MAIN">
        <menu id="A"
              description="F">
            <item id="A1"
                  screen="test_Order.browse"/>
        </menu>

        <separator/>

        <menu id="B">
            <item id="B1"
                  screen="test_Order.browse"/>
        </menu>
    </menu>

    <separator/>
    
    <item id="B2"
          screen="sec$User.browse"/>
</menu-config>
'''.trim())

        when: "we build UI menu structure"
        builder.build(menu, menuConfig.rootItems)

        then: "menu should not contain separators"
        menu.menuItems.size() == 2
        menu.menuItems

        menu.menuItems[0].children.size() == 2
    }

    def "sidemenu builder should replace item from root menu if it has insertAfter or insertBefore"() {

        given: "menu loads XML with separator and item in the root menu"

        def mainScreen = showTestMainScreen()
        def menu = uiComponents.create(SideMenu)
        menu.frame = mainScreen.window

        menuConfig.loadTestMenu('''
<menu-config xmlns="http://jmix.io/schema/ui/menu">
    <menu id="MAIN">
        <menu id="A"
              description="F">
            <item id="A1"
                  screen="test_Order.browse"/>
            <item id="A2"
                  screen="test_Order.browse"/>
        </menu>
        
        <separator/>

        <menu id="B">
            <item id="B1"
                  screen="test_Order.browse"/>
        </menu>
    </menu>
    
    <separator insertBefore="A2"/>
    
    <item id="B2"
          screen="test_Order.browse" 
          insertAfter="B1"/>
</menu-config>
'''.trim())

        when: "we build UI menu structure"
        builder.build(menu, menuConfig.rootItems)

        then: "root menu should contains only one child"
        menu.menuItems.size() == 1

        and: "separator in the menu MAIN should be removed"

        def children = menu.menuItems[0].children
        children.size() == 2

        and: "item B2 should be inserted after B1"

        def childrenB = children[1].children

        childrenB.size() == 2
        childrenB[0].id == "B1"
        childrenB[1].id == "B2"
    }
}

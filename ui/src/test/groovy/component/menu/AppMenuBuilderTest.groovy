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
import io.jmix.ui.component.mainwindow.AppMenu
import io.jmix.ui.testassist.spec.ScreenSpecification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.ContextConfiguration
import test_support.UiTestConfiguration
import test_support.bean.TestAppMenuBuilder
import test_support.bean.TestMenuConfig

@ContextConfiguration(classes = [CoreConfiguration, UiConfiguration, DataConfiguration,
        EclipselinkConfiguration, UiTestConfiguration])
class AppMenuBuilderTest extends ScreenSpecification {

    @Autowired
    TestMenuConfig menuConfig
    @Autowired
    TestAppMenuBuilder builder

    def "menu builder removes extra empty submenus"() {
        given: "menu loads XML with extra empty submenu"

        def mainScreen = showTestMainScreen()
        def menu = uiComponents.create(AppMenu)
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
        menu.getMenuItems().size() == 1
        menu.getMenuItems()[0].children.size() == 2
    }

    @SuppressWarnings(['GroovyPointlessBoolean', 'GroovyAccessibility'])
    def "menu builder does not support top-level separators"() {
        given: "menu loads XML with extra separators on top level"

        def mainScreen = showTestMainScreen()
        def menu = uiComponents.create(AppMenu)
        menu.frame = mainScreen.window

        menuConfig.loadTestMenu('''
<menu-config xmlns="http://jmix.io/schema/ui/menu">
    <menu id="A"
          description="F">
        <item id="A1"
              screen="test_Order.browse"/>
    </menu>
    
    <separator/>

    <item id="B1"
          screen="test_Order.browse"/>
</menu-config>
'''.trim())

        when: "we build UI menu structure"
        builder.build(menu, menuConfig.rootItems)

        then: "top level menu does not contain separator"
        menu.menuItems.size() == 2
        menu.menuItems[1].separator == false
        menu.menuItems[0].separator == false
    }

    @SuppressWarnings(["GroovyPointlessBoolean", 'GroovyAccessibility'])
    def "menu builder removes extra separator elements"() {

        given: "menu loads XML with extra separators"

        def mainScreen = showTestMainScreen()
        def menu = uiComponents.create(AppMenu)
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
        
        <separator/>

        <menu id="B">
            <item id="B1"
                  screen="test_Order.browse"/>
        </menu>
    </menu>
</menu-config>
'''.trim())

        when: "we build UI menu structure"
        builder.build(menu, menuConfig.rootItems)

        then: "menu should contain only one separator element"
        menu.menuItems.size() == 1

        def children = menu.menuItems[0].children

        children.size() == 3
        children[1].separator == true
        children[0].separator == false
        children[2].separator == false
    }

    @SuppressWarnings(['GroovyAccessibility', 'GroovyPointlessBoolean'])
    def "menu should replace item and separator from root menu if they have insertAfter or insertBefore"() {

        given: "menu loads XML with separator and item in the root menu"

        def mainScreen = showTestMainScreen()
        def menu = uiComponents.create(AppMenu)
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

        and: "separator should be inserted after A1"

        def children = menu.menuItems[0].children
        children.size() == 3

        def childrenA = children[0].children

        childrenA.size() == 3
        childrenA[0].separator == false
        childrenA[1].separator == true
        childrenA[2].separator == false

        and: "item B2 should be inserted after B1"

        def childrenB = children[2].children

        childrenB.size() == 2
        childrenB[0].id == "B1"
        childrenB[1].id == "B2"
    }
}

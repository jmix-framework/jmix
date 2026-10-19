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
import com.vaadin.flow.router.AfterNavigationEvent
import com.vaadin.flow.router.Location
import com.vaadin.flow.router.LocationChangeEvent
import com.vaadin.flow.router.NavigationTrigger
import com.vaadin.flow.router.RouterLink
import component.listmenu.test_support.ListMenuTestConfiguration
import component.listmenu.test_support.TestMenuConfig
import component.listmenu.view.ListMenuTestView
import io.jmix.flowui.UiComponents
import io.jmix.flowui.component.main.JmixListMenu
import io.jmix.flowui.kit.component.main.ListMenu
import io.jmix.flowui.menu.ListMenuBuilder
import io.jmix.flowui.menu.MenuItem.MenuItemParameter
import io.jmix.flowui.menu.provider.MenuConfigListMenuItemProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.ComponentTestUtils
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(classes = [ListMenuTestConfiguration])
class ListMenuTest extends FlowuiTestSpecification {

    static final String ADMIN_MENU_WITH_HIDDEN_ITEMS = '''
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
        '''.trim()

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

    def "Open parent menu of highlighted RouterLink after navigation event"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)

        def applicationMenuBar = ListMenu.MenuItem.createMenuBar("application")
        applicationMenuBar.addChildItem(JmixListMenu.ViewMenuItem.create("ListMenuTestView"))

        listMenu.addMenuItem(applicationMenuBar)
        ui.add(listMenu)

        and:
        !applicationMenuBar.opened

        when:
        fireAfterNavigation(listMenu, "ListMenuTestView")

        then:
        applicationMenuBar.opened
        getRenderedMenuItemComponent(listMenu, "ListMenuTestView").element.hasAttribute("highlight")
    }

    def "Open only parent menu of highlighted RouterLink when same view has several menu items"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)

        def matchingMenuBar = ListMenu.MenuItem.createMenuBar("matching")
        matchingMenuBar.addChildItem(new JmixListMenu.ViewMenuItem("matchingView")
                .withControllerClass(ListMenuTestView)
                .withTitle("Matching view"))

        def queryMenuBar = ListMenu.MenuItem.createMenuBar("query")
        queryMenuBar.addChildItem(new JmixListMenu.ViewMenuItem("queryView")
                .withControllerClass(ListMenuTestView)
                .withUrlQueryParameters([new MenuItemParameter("mode", "query")])
                .withTitle("Query view"))

        listMenu.addMenuItem(matchingMenuBar)
        listMenu.addMenuItem(queryMenuBar)
        ui.add(listMenu)

        and:
        !matchingMenuBar.opened
        !queryMenuBar.opened

        when:
        fireAfterNavigation(listMenu, "ListMenuTestView")

        then:
        matchingMenuBar.opened
        !queryMenuBar.opened
        getRenderedMenuItemComponent(listMenu, "matchingView").element.hasAttribute("highlight")
        !getRenderedMenuItemComponent(listMenu, "queryView").element.hasAttribute("highlight")
    }

    def "Open parent menus of all RouterLinks matching navigation event"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)

        def firstMenuBar = ListMenu.MenuItem.createMenuBar("first")
        firstMenuBar.addChildItem(new JmixListMenu.ViewMenuItem("firstView")
                .withControllerClass(ListMenuTestView)
                .withTitle("First view"))

        def secondMenuBar = ListMenu.MenuItem.createMenuBar("second")
        secondMenuBar.addChildItem(new JmixListMenu.ViewMenuItem("secondView")
                .withControllerClass(ListMenuTestView)
                .withTitle("Second view"))

        listMenu.addMenuItem(firstMenuBar)
        listMenu.addMenuItem(secondMenuBar)
        ui.add(listMenu)

        and:
        !firstMenuBar.opened
        !secondMenuBar.opened

        when:
        fireAfterNavigation(listMenu, "ListMenuTestView")

        then:
        firstMenuBar.opened
        secondMenuBar.opened
        getRenderedMenuItemComponent(listMenu, "firstView").element.hasAttribute("highlight")
        getRenderedMenuItemComponent(listMenu, "secondView").element.hasAttribute("highlight")
    }

    def "Apply visibility of a menu item changed after it is added"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        listMenu.addMenuItem(ListMenu.MenuItem.create("first"))

        when:
        listMenu.getMenuItem("first").setVisible(false)

        then:
        !getRenderedMenuItemComponent(listMenu, "first").visible

        when:
        listMenu.getMenuItem("first").setVisible(true)

        then:
        getRenderedMenuItemComponent(listMenu, "first").visible
    }

    def "Apply visibility of a menu bar changed after it is added"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)

        def menuBar = ListMenu.MenuItem.createMenuBar("bar")
        menuBar.addChildItem(ListMenu.MenuItem.create("child"))
        listMenu.addMenuItem(menuBar)

        when:
        listMenu.getMenuItem("bar").setVisible(false)

        then:
        !getRenderedMenuItemComponent(listMenu, "bar").visible

        when:
        listMenu.getMenuItem("bar").setVisible(true)

        then:
        getRenderedMenuItemComponent(listMenu, "bar").visible
    }

    def "Render an item hidden before it is added as hidden"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)

        when:
        listMenu.addMenuItem(ListMenu.MenuItem.create("first").withVisible(false))

        then:
        !getRenderedMenuItemComponent(listMenu, "first").visible
    }

    def "Hide separators left after hidden items: the ticket's example via ListMenuBuilder"() {
        given:
        menuConfig.loadTestMenu(ADMIN_MENU_WITH_HIDDEN_ITEMS)
        def listMenu = uiComponents.create(JmixListMenu)

        when:
        listMenuBuilder.build(listMenu)

        then:
        def adminBar = (ListMenu.MenuBarItem) listMenu.getMenuItem("admin")
        adminBar.childItems.count { it.separator } == 2
        shownSeparators(listMenu, adminBar.childItems) == []
    }

    def "Hide separators left after hidden items: the ticket's example via MenuConfigListMenuItemProvider"() {
        given:
        menuConfig.loadTestMenu(ADMIN_MENU_WITH_HIDDEN_ITEMS)
        def listMenu = uiComponents.create(JmixListMenu)
        def provider = applicationContext.getBean(MenuConfigListMenuItemProvider)
        listMenu.setMenuItemProvider(provider)

        when:
        provider.load()

        then:
        def adminBar = (ListMenu.MenuBarItem) listMenu.getMenuItem("admin")
        adminBar.childItems.count { it.separator } == 2
        shownSeparators(listMenu, adminBar.childItems) == []
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
        def listMenu = uiComponents.create(JmixListMenu)

        when:
        listMenuBuilder.build(listMenu)

        then:
        listMenu.getMenuItem("admin") == null
    }

    def "Hide leading, trailing and consecutive separators at the root"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)

        when:
        [separator("s1"), item("a"), separator("s2"), separator("s3"), item("b"), separator("s4")]
                .each { listMenu.addMenuItem(it) }

        then:
        shownSeparators(listMenu, listMenu.menuItems) == ["s2"]
    }

    def "Hide leading, trailing and consecutive separators in a submenu"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def bar = menuBar("bar", separator("s1"), item("a"), separator("s2"), separator("s3"), item("b"),
                separator("s4"))

        when:
        listMenu.addMenuItem(bar)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s2"]
    }

    def "Keep separators between visible items"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def bar = menuBar("bar", item("a"), separator("s1"), item("b"), separator("s2"), item("c"))

        when:
        listMenu.addMenuItem(bar)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s1", "s2"]
    }

    def "Recompute separators when an item visibility changes"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def b = item("b")
        def bar = menuBar("bar", item("a"), separator("s1"), b, separator("s2"), item("c"))
        listMenu.addMenuItem(bar)

        when:
        b.setVisible(false)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s1"]

        when:
        b.setVisible(true)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s1", "s2"]
    }

    def "Level without visible items hides all separators"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)

        when:
        [item("a").withVisible(false), separator("s1"), item("b").withVisible(false)]
                .each { listMenu.addMenuItem(it) }

        then:
        shownSeparators(listMenu, listMenu.menuItems) == []
    }

    def "Keep a separator hidden by the developer hidden"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def s1 = separator("s1")
        def bar = menuBar("bar", item("a"), s1, item("b"))
        listMenu.addMenuItem(bar)

        when:
        s1.setVisible(false)

        then:
        shownSeparators(listMenu, bar.childItems) == []
        !s1.visible

        when:
        s1.setVisible(true)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s1"]
    }

    def "Hidden first separator of a run is replaced by the next one"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def bar = menuBar("bar", item("a"), separator("s1").withVisible(false), separator("s2"), item("b"))

        when:
        listMenu.addMenuItem(bar)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s2"]
    }

    def "A separator the menu hid still reports the developer's value"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def s1 = separator("s1")

        when:
        listMenu.addMenuItem(s1)
        listMenu.addMenuItem(item("a"))

        then:
        !findElementById(listMenu.element, "s1").visible
        s1.visible
    }

    def "Recompute separators when root items are added and removed"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        listMenu.addMenuItem(item("a"))
        listMenu.addMenuItem(separator("s1"))

        expect:
        shownSeparators(listMenu, listMenu.menuItems) == []

        when:
        def b = item("b")
        listMenu.addMenuItem(b)

        then:
        shownSeparators(listMenu, listMenu.menuItems) == ["s1"]

        when:
        listMenu.removeMenuItem(b)

        then:
        shownSeparators(listMenu, listMenu.menuItems) == []

        when:
        listMenu.addMenuItem(item("c"), 2)

        then:
        shownSeparators(listMenu, listMenu.menuItems) == ["s1"]

        when:
        listMenu.removeMenuItem(0)

        then:
        shownSeparators(listMenu, listMenu.menuItems) == []
    }

    def "Recompute separators when submenu children are added and removed"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def bar = menuBar("bar", item("a"), separator("s1"))
        listMenu.addMenuItem(bar)

        expect:
        shownSeparators(listMenu, bar.childItems) == []

        when:
        def b = item("b")
        bar.addChildItem(b)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s1"]

        when:
        bar.removeChildItem(b)

        then:
        shownSeparators(listMenu, bar.childItems) == []

        when:
        bar.addChildItem(b, 2)

        then:
        shownSeparators(listMenu, bar.childItems) == ["s1"]

        when:
        bar.removeChildItem(2)

        then:
        shownSeparators(listMenu, bar.childItems) == []
    }

    def "Recompute separators when a nested item is removed from the menu directly"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def b = item("b")
        def bar = menuBar("bar", item("a"), separator("s1"), b)
        listMenu.addMenuItem(bar)

        expect:
        shownSeparators(listMenu, bar.childItems) == ["s1"]

        when:
        listMenu.removeMenuItem(b)

        then:
        noExceptionThrown()
        shownSeparators(listMenu, bar.childItems) == []
    }

    def "Moving an item recomputes separators"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def barA = item("barA")
        def bar = menuBar("bar", barA, separator("barS1"), item("barB"))
        def rootA = item("rootA")
        listMenu.addMenuItem(rootA)
        listMenu.addMenuItem(separator("rootS1"))
        listMenu.addMenuItem(item("rootB"))
        listMenu.addMenuItem(bar)

        expect:
        shownSeparators(listMenu, bar.childItems) == ["barS1"]
        shownSeparators(listMenu, listMenu.menuItems) == ["rootS1"]

        when:
        bar.addChildItem(barA, 3)

        then:
        bar.childItems*.id == ["barS1", "barB", "barA"]
        shownSeparators(listMenu, bar.childItems) == []

        when:
        listMenu.addMenuItem(rootA, 3)

        then:
        listMenu.menuItems*.id == ["rootS1", "rootB", "rootA", "bar"]
        shownSeparators(listMenu, listMenu.menuItems) == []
    }

    def "Item hidden before add is rendered hidden and separators follow it"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def bar = menuBar("bar", item("a"), separator("s1"), item("b").withVisible(false))

        when:
        listMenu.addMenuItem(bar)

        then:
        !getRenderedMenuItemComponent(listMenu, "b").visible
        shownSeparators(listMenu, bar.childItems) == []
    }

    def "Nested submenu level is recomputed"() {
        given:
        def listMenu = uiComponents.create(JmixListMenu)
        def b = item("b")
        def inner = menuBar("inner", item("a"), separator("s1"), b)
        listMenu.addMenuItem(menuBar("outer", item("x"), inner))

        expect:
        shownSeparators(listMenu, inner.childItems) == ["s1"]

        when:
        b.setVisible(false)

        then:
        shownSeparators(listMenu, inner.childItems) == []
    }

    protected static ListMenu.MenuItem item(String id) {
        return ListMenu.MenuItem.create(id)
    }

    protected static ListMenu.MenuItem separator(String id) {
        return new ListMenu.MenuSeparatorItem(id)
    }

    protected static ListMenu.MenuBarItem menuBar(String id, ListMenu.MenuItem... children) {
        def menuBar = ListMenu.MenuItem.createMenuBar(id)
        children.each { menuBar.addChildItem(it) }
        return menuBar
    }

    protected static List<String> shownSeparators(JmixListMenu listMenu, List<ListMenu.MenuItem> level) {
        return level.findAll { it.separator && findElementById(listMenu.element, it.id)?.visible }
                .collect { it.id }
    }

    protected void fireAfterNavigation(JmixListMenu listMenu, String path) {
        def event = new AfterNavigationEvent(new LocationChangeEvent(
                ui.getInternals().getRouter(),
                ui,
                NavigationTrigger.PROGRAMMATIC,
                new Location(path),
                Collections.emptyList()
        ))

        listMenu.afterNavigation(event)
        getRenderedRouterLinks(listMenu).forEach { routerLink -> routerLink.afterNavigation(event) }
    }

    protected static List<RouterLink> getRenderedRouterLinks(JmixListMenu listMenu) {
        List<RouterLink> routerLinks = []
        collectRenderedRouterLinks(listMenu.element, routerLinks)
        return routerLinks
    }

    protected static void collectRenderedRouterLinks(Element element, List<RouterLink> routerLinks) {
        element.component.ifPresent { component ->
            if (component instanceof RouterLink) {
                routerLinks.add((RouterLink) component)
            }
        }

        element.children.forEach { child -> collectRenderedRouterLinks(child, routerLinks) }
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

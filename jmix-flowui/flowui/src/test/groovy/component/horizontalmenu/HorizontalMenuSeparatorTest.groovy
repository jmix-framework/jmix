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

import io.jmix.flowui.component.horizontalmenu.HorizontalMenu
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class HorizontalMenuSeparatorTest extends FlowuiTestSpecification {

    def "Hide the wrapper of a submenu item hidden after it is added"() {
        given:
        def menu = new HorizontalMenu()
        def a = item("a")
        menu.addMenuItem(parent("admin", a, item("b")))

        when:
        a.setVisible(false)

        then:
        !a.menuItemWrapper.visible
        !a.visible

        when:
        a.setVisible(true)

        then:
        a.menuItemWrapper.visible
        a.visible
    }

    def "Hide the wrapper of a root item hidden after it is added"() {
        given:
        def menu = new HorizontalMenu()
        def a = item("a")
        menu.addMenuItem(a)

        when:
        a.setVisible(false)

        then:
        !a.menuItemWrapper.visible
        !a.visible
    }

    def "Item hidden before add starts with a hidden wrapper"() {
        given:
        def menu = new HorizontalMenu()
        def a = item("a")
        a.setVisible(false)

        when:
        menu.addMenuItem(parent("admin", a, item("b")))

        then:
        !a.menuItemWrapper.visible
    }

    def "Hide leading, trailing and consecutive separators in a submenu"() {
        given:
        def menu = new HorizontalMenu()
        def admin = parent("admin", separator("s1"), item("a"), separator("s2"), separator("s3"), item("b"),
                separator("s4"))

        when:
        menu.addMenuItem(admin)

        then:
        shownSeparators(admin) == ["s2"]
    }

    def "Keep separators between visible items"() {
        given:
        def menu = new HorizontalMenu()
        def admin = parent("admin", item("a"), separator("s1"), item("b"), separator("s2"), item("c"))

        when:
        menu.addMenuItem(admin)

        then:
        shownSeparators(admin) == ["s1", "s2"]
    }

    def "Recompute separators when an item visibility changes"() {
        given:
        def menu = new HorizontalMenu()
        def b = item("b")
        def admin = parent("admin", item("a"), separator("s1"), b, separator("s2"), item("c"))
        menu.addMenuItem(admin)

        when:
        b.setVisible(false)

        then:
        shownSeparators(admin) == ["s1"]

        when:
        b.setVisible(true)

        then:
        shownSeparators(admin) == ["s1", "s2"]
    }

    def "Level without visible items hides all separators"() {
        given:
        def menu = new HorizontalMenu()
        def a = item("a")
        a.setVisible(false)
        def b = item("b")
        b.setVisible(false)
        def admin = parent("admin", a, separator("s1"), b)

        when:
        menu.addMenuItem(admin)

        then:
        shownSeparators(admin) == []
    }

    def "Keep a separator hidden by the developer hidden"() {
        given:
        def menu = new HorizontalMenu()
        def s1 = separator("s1")
        def admin = parent("admin", item("a"), s1, item("b"))
        menu.addMenuItem(admin)

        when:
        s1.setVisible(false)

        then:
        shownSeparators(admin) == []
        !s1.visible

        when:
        s1.setVisible(true)

        then:
        shownSeparators(admin) == ["s1"]
        s1.visible
    }

    def "Hidden first separator of a run is replaced by the next one"() {
        given:
        def menu = new HorizontalMenu()
        def s1 = separator("s1")
        s1.setVisible(false)
        def admin = parent("admin", item("a"), s1, separator("s2"), item("b"))

        when:
        menu.addMenuItem(admin)

        then:
        shownSeparators(admin) == ["s2"]
    }

    def "A separator the menu hid still reports the developer's value"() {
        given:
        def menu = new HorizontalMenu()
        def s1 = separator("s1")

        when:
        menu.addMenuItem(parent("admin", s1, item("a")))

        then:
        !s1.element.visible
        s1.visible
    }

    def "Recompute separators when submenu children are added and removed"() {
        given:
        def menu = new HorizontalMenu()
        def admin = parent("admin", item("a"), separator("s1"))
        menu.addMenuItem(admin)

        expect:
        shownSeparators(admin) == []

        when:
        def b = item("b")
        admin.addChildItem(b)

        then:
        shownSeparators(admin) == ["s1"]

        when:
        admin.removeChildItem(b)

        then:
        shownSeparators(admin) == []

        when:
        admin.addChildItem(b, 2)

        then:
        shownSeparators(admin) == ["s1"]

        when:
        admin.removeChildItem(2)

        then:
        shownSeparators(admin) == []
    }

    def "Remove a separator from the rendered submenu"() {
        given:
        def menu = new HorizontalMenu()
        def s1 = separator("s1")
        def admin = parent("admin", item("a"), s1, item("b"))
        menu.addMenuItem(admin)

        expect:
        admin.menuItemWrapper.subMenu.children.anyMatch { it == s1 }

        when:
        admin.removeChildItem(s1)

        then:
        admin.menuItemWrapper.subMenu.children.noneMatch { it == s1 }
    }

    def "Clear the parent of a removed item"() {
        given:
        def menu = new HorizontalMenu()
        def b = item("b")
        def admin = parent("admin", item("a"), separator("s1"), b)
        menu.addMenuItem(admin)

        when:
        admin.removeChildItem(b)

        then:
        b.parentMenuItem == null
    }

    def "Item hidden before add is accounted for"() {
        given:
        def menu = new HorizontalMenu()
        def b = item("b")
        b.setVisible(false)
        def admin = parent("admin", item("a"), separator("s1"), b)

        when:
        menu.addMenuItem(admin)

        then:
        shownSeparators(admin) == []
    }

    def "Nested submenu level is recomputed"() {
        given:
        def menu = new HorizontalMenu()
        def b = item("b")
        def inner = parent("inner", item("a"), separator("s1"), b)
        menu.addMenuItem(parent("outer", item("x"), inner))

        expect:
        shownSeparators(inner) == ["s1"]

        when:
        b.setVisible(false)

        then:
        shownSeparators(inner) == []
    }

    protected static List<String> shownSeparators(HorizontalMenu.ParentMenuItem parent) {
        return parent.childItems.findAll { it.separator && it.element.visible }
                .collect { it.id.get() }
    }

    protected static HorizontalMenu.MenuItem item(String id) {
        return new HorizontalMenu.MenuItem(id)
    }

    protected static HorizontalMenu.SeparatorMenuItem separator(String id) {
        return new HorizontalMenu.SeparatorMenuItem(id)
    }

    protected static HorizontalMenu.ParentMenuItem parent(String id, HorizontalMenu.AbstractMenuItem<?>... children) {
        def parent = new HorizontalMenu.ParentMenuItem(id)
        children.each { parent.addChildItem(it) }
        return parent
    }
}

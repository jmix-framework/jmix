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

package menu


import io.jmix.flowui.kit.component.KeyCombination
import io.jmix.flowui.menu.MenuConfig
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest(["jmix.ui.composite-menu=false", "jmix.ui.menu-config=menu/menu.xml"])
class MenuConfigTest extends FlowuiTestSpecification {

    @Autowired
    MenuConfig menuConfig

    def "Load menu form XML with nested menu"() {
        when:
        def rootItems = menuConfig.getRootItems()
        then:

        rootItems.size() == 2

        def applicationMenu = rootItems.get(0)
        applicationMenu.getId() == "application"
        applicationMenu.getBean() == null
        applicationMenu.getBeanMethod() == null
        applicationMenu.getClassNames() == null
        applicationMenu.getDescription() == "Application"
        applicationMenu.getIcon() == "TABLE"
        applicationMenu.getParent() == null
        applicationMenu.getProperties() == []
        applicationMenu.getRouteParameters() == []
        applicationMenu.getUrlQueryParameters() == []
        applicationMenu.getTitle() == "Application title"
        applicationMenu.getView() == null
        applicationMenu.getShortcutCombination() == null
        applicationMenu.isMenu()
        applicationMenu.isOpened()
        !applicationMenu.isSeparator()
        def applicationMenuChildren = applicationMenu.getChildren()
        applicationMenuChildren.size() == 2

        def applicationView = applicationMenuChildren.get(0)
        applicationView.getId() == "Application.view"
        applicationView.getBean() == null
        applicationView.getBeanMethod() == null
        applicationView.getClassNames() == null
        applicationView.getDescription() == "app view"
        applicationView.getIcon() == "ABACUS"
        applicationView.getParent() == applicationMenu
        applicationView.getProperties() == []
        applicationView.getRouteParameters() == []
        applicationView.getUrlQueryParameters() == []
        applicationView.getTitle() == "Application view"
        applicationView.getView() == "Application.view"
        applicationView.getShortcutCombination() == KeyCombination.create("Control-Enter")
        !applicationView.isMenu()
        !applicationView.isOpened()
        !applicationView.isSeparator()
        applicationView.getChildren() == []

        def nestedMenu = applicationMenuChildren.get(1)
        nestedMenu.getId() == "nestedMenu"
        nestedMenu.getBean() == null
        nestedMenu.getBeanMethod() == null
        nestedMenu.getClassNames() == null
        nestedMenu.getDescription() == null
        nestedMenu.getIcon() == null
        nestedMenu.getParent() == applicationMenu
        nestedMenu.getProperties() == []
        nestedMenu.getRouteParameters() == []
        nestedMenu.getUrlQueryParameters() == []
        nestedMenu.getTitle() == "Nested menu"
        nestedMenu.getView() == null
        nestedMenu.getShortcutCombination() == null
        nestedMenu.isMenu()
        !nestedMenu.isOpened()
        !nestedMenu.isSeparator()
        def nestedMenuChildren = nestedMenu.getChildren()
        nestedMenuChildren.size() == 1

        def nestedView = nestedMenuChildren.get(0)
        nestedView.getId() == "Nested.view"
        nestedView.getBean() == null
        nestedView.getBeanMethod() == null
        nestedView.getClassNames() == null
        nestedView.getDescription() == null
        nestedView.getIcon() == null
        nestedView.getParent() == nestedMenu
        nestedView.getProperties() == []
        nestedView.getRouteParameters() == []
        nestedView.getUrlQueryParameters() == []
        nestedView.getTitle() == "Nested view"
        nestedView.getView() == "Nested.view"
        nestedView.getShortcutCombination() == null
        !nestedView.isMenu()
        !nestedView.isOpened()
        !nestedView.isSeparator()
        nestedView.getChildren() == []

        def administrationMenu = rootItems.get(1)
        administrationMenu.getId() == "administration"
        administrationMenu.getBean() == null
        administrationMenu.getBeanMethod() == null
        administrationMenu.getClassNames() == null
        administrationMenu.getDescription() == null
        administrationMenu.getIcon() == null
        administrationMenu.getParent() == null
        administrationMenu.getProperties() == []
        administrationMenu.getRouteParameters() == []
        administrationMenu.getUrlQueryParameters() == []
        administrationMenu.getTitle() == "Administration"
        administrationMenu.getView() == null
        administrationMenu.getShortcutCombination() == null
        administrationMenu.isMenu()
        !administrationMenu.isOpened()
        !administrationMenu.isSeparator()
        def administrationMenuChildren = administrationMenu.getChildren()
        administrationMenuChildren.size() == 1

        def administrationView = administrationMenuChildren.get(0)
        administrationView.getId() == "Administration.view"
        administrationView.getBean() == null
        administrationView.getBeanMethod() == null
        administrationView.getClassNames() == null
        administrationView.getDescription() == null
        administrationView.getIcon() == null
        administrationView.getParent() == administrationMenu
        administrationView.getProperties() == []
        administrationView.getRouteParameters() == []
        administrationView.getUrlQueryParameters() == []
        administrationView.getTitle() == "Administration view"
        administrationView.getView() == "Administration.view"
        administrationView.getShortcutCombination() == null
        !administrationView.isMenu()
        !administrationView.isOpened()
        !administrationView.isSeparator()
        administrationView.getChildren() == []
    }
}

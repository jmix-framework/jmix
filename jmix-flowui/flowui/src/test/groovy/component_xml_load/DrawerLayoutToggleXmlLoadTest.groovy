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

package component_xml_load

import com.vaadin.flow.component.icon.FontIcon
import component_xml_load.screen.DrawerLayoutToggleView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class DrawerLayoutToggleXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load DrawerLayoutToggle attributes"() {
        when: "Open the view"
        def view = navigateToView(DrawerLayoutToggleView)

        then: "Check all attributes"
        verifyAll(view.drawerLayoutToggle) {
            id.get() == "drawerLayoutToggle"
            ariaLabel.orElse(null) == "ariaLabel"
            autofocus
            classNames.containsAll(["className1", "className2"])
            style.get("color") == "red"
            drawerLayout != null
            height == "100px"
            icon != null
            maxHeight == "100px"
            maxWidth == "100px"
            minHeight == "100px"
            minWidth == "100px"
            tabIndex == 2
            themeNames.containsAll(["primary", "icon"])
            visible
            width == "100px"
        }

        when: "Retrieve an icon"
        FontIcon icon = (FontIcon) view.drawerLayoutToggle.icon;

        then: "Check icon name"
        icon.iconClassNames.collect().containsAll("jmix-font-icon", "jmix-font-icon-abacus")
    }

    def "Check setting DrawerLayout to toggle without specifying ID"() {
        when: "Open the view"
        def view = navigateToView(DrawerLayoutToggleView)

        then: "Check that DrawerLayout is set"

        view.innerDrawerLayoutToggle.drawerLayout != null
    }
}

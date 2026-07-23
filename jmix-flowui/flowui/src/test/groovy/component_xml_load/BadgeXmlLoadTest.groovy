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
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import component_xml_load.screen.BadgeView
import io.jmix.flowui.kit.icon.JmixFontIcon
import org.springframework.boot.test.context.SpringBootTest
import test_support.ComponentTestUtils
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class BadgeXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load badge attributes from XML"() {
        when: "Open the BadgeView"
        def view = navigateToView(BadgeView.class)

        then: "Badge attributes are loaded"
        verifyAll(view.badge) {
            text == "Badge text"
            number == 5
            role == "status"
            themeNames.containsAll(["success", "small"])
            className == "className1"
            style.get("color") == "red"
            !visible
            height == "10px"
            maxHeight == "10px"
            maxWidth == "10px"
            minHeight == "10px"
            minWidth == "10px"
            width == "10px"
        }
    }

    def "Load badge icon from icon element"() {
        when: "Open the BadgeView"
        def view = navigateToView(BadgeView.class)

        then: "Icon component is loaded into the icon slot"
        view.badgeIconSlot.icon instanceof Icon
        ComponentTestUtils.isSameIcon(view.badgeIconSlot.icon, VaadinIcon.CHECK)
    }

    def "Load badge icon from icon attribute"() {
        when: "Open the BadgeView"
        def view = navigateToView(BadgeView.class)

        then: "Icon is loaded from the icon attribute"
        view.badgeIconAttribute.icon instanceof FontIcon
        ComponentTestUtils.isSameIcon(view.badgeIconAttribute.icon, JmixFontIcon.CHECK)
    }
}

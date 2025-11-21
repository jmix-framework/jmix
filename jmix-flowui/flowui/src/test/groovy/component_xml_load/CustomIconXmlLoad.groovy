/*
 * Copyright 2025 Haulmont.
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


import com.vaadin.flow.component.html.Image
import com.vaadin.flow.component.icon.FontIcon
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.SvgIcon
import com.vaadin.flow.component.icon.VaadinIcon
import component_xml_load.screen.CustomIconView
import io.jmix.flowui.kit.icon.JmixFontIcon
import org.springframework.boot.test.context.SpringBootTest
import test_support.ComponentTestUtils
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class CustomIconXmlLoad extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load Icon as icon (attribute)"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Action has Icon as icon"
        view.iconAttributeAction.iconComponent instanceof FontIcon
        ComponentTestUtils.isSameIcon(view.iconAttributeAction.iconComponent, JmixFontIcon.CHECK)

        and: "Button has Icon as icon"
        view.iconAttributeButton.icon instanceof FontIcon
        ComponentTestUtils.isSameIcon(view.iconAttributeButton.icon, JmixFontIcon.CHECK)
    }

    def "Load Icon as Vaadin icon (attribute)"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Action has Icon as icon"
        view.vaadinIconAttributeAction.iconComponent instanceof Icon
        ComponentTestUtils.isSameIcon(view.vaadinIconAttributeAction.iconComponent, VaadinIcon.CHECK)
        ComponentTestUtils.isSameIcon(view.vaadinIconAttributeAction.icon, VaadinIcon.CHECK)

        and: "Button has Icon as icon"
        view.vaadinIconAttributeButton.icon instanceof Icon
        ComponentTestUtils.isSameIcon(view.vaadinIconAttributeButton.icon, VaadinIcon.CHECK)
    }

    def "Load Icon as icon"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Action has Icon as icon"
        view.iconAction.iconComponent instanceof Icon
        ComponentTestUtils.isSameIcon(view.iconAction.iconComponent, VaadinIcon.CHECK)
        ComponentTestUtils.isSameIcon(view.iconAction.icon, VaadinIcon.CHECK)

        and: "Button has Icon as icon"
        view.iconButton.icon instanceof Icon
        ComponentTestUtils.isSameIcon(view.iconButton.icon, VaadinIcon.CHECK)
    }

    def "Load SvgIcon as icon"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Action has SvgIcon as icon"
        view.svgIconAction.iconComponent instanceof SvgIcon
        (view.svgIconAction.iconComponent as SvgIcon).src == "/icons/check-solid-full.svg"

        and: "Button has SvgIcon as icon"
        view.svgIconButton.icon instanceof SvgIcon
        (view.svgIconButton.icon as SvgIcon).src == "/icons/check-solid-full.svg"
    }

    def "Load FontIcon as icon"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Action has FontIcon as icon"
        view.fontIconAction.iconComponent instanceof FontIcon
        ComponentTestUtils.isSameFontIcon(view.fontIconAction.iconComponent as FontIcon,
                "lumo-icons", "")

        and: "Button has FontIcon as icon"
        view.fontIconButton.icon instanceof FontIcon
        ComponentTestUtils.isSameFontIcon(view.fontIconButton.icon as FontIcon,
                "lumo-icons", "")
    }

    def "Load Image as icon"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Action has Image as icon"
        view.imageAction.iconComponent instanceof Image
        (view.imageAction.iconComponent as Image).src == "/icons/icon.png"

        and: "Button has Image as icon"
        view.imageButton.icon instanceof Image
        (view.imageButton.icon as Image).src == "/icons/icon.png"
    }

    def "Button get icon from Action"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Button get icon (attribute) from Action"
        view.iconAttributeActionButton.icon instanceof FontIcon
        ComponentTestUtils.isSameIcon(view.iconAttributeActionButton.icon, JmixFontIcon.CHECK)

        then: "Button get Vaadin icon (attribute) from Action"
        view.vaadinIconAttributeActionButton.icon instanceof Icon
        ComponentTestUtils.isSameIcon(view.vaadinIconAttributeActionButton.icon, VaadinIcon.CHECK)

        and: "Button get Icon as icon from Action"
        view.iconActionButton.icon instanceof Icon
        ComponentTestUtils.isSameIcon(view.iconActionButton.icon, VaadinIcon.CHECK)

        and: "Button get SvgIcon as icon from Action"
        view.svgIconActionButton.icon instanceof SvgIcon
        (view.svgIconActionButton.icon as SvgIcon).src == "/icons/check-solid-full.svg"

        and: "Button get FontIcon as icon from Action"
        view.fontIconActionButton.icon instanceof FontIcon
        ComponentTestUtils.isSameFontIcon(view.fontIconActionButton.icon as FontIcon,
                "lumo-icons", "")

        and: "Button get Image as icon from Action"
        view.imageActionButton.icon instanceof Image
        (view.imageActionButton.icon as Image).src == "/icons/icon.png"
    }

    def "Button overrides icon from Action"() {
        when: "Open the CustomIconView"
        def view = navigateToView(CustomIconView)

        then: "Button overrides icon (attribute) from Action"
        view.overrideIconAttributeActionButton.icon instanceof FontIcon
        ComponentTestUtils.isSameIcon(view.overrideIconAttributeActionButton.icon, JmixFontIcon.CLOSE)

        then: "Button overrides vaadin icon (attribute) from Action"
        view.overrideVaadinIconAttributeActionButton.icon instanceof Icon
        ComponentTestUtils.isSameIcon(view.overrideVaadinIconAttributeActionButton.icon, VaadinIcon.CLOSE)

        and: "Button overrides Icon as icon from Action"
        view.overrideIconActionButton.icon instanceof Icon
        ComponentTestUtils.isSameIcon(view.overrideIconActionButton.icon, VaadinIcon.CLOSE)

        and: "Button overrides FontIcon as icon from Action"
        view.overrideSvgIconActionButton.icon instanceof SvgIcon
        def svgIcon = view.overrideSvgIconActionButton.icon as SvgIcon
        svgIcon.src == "/icons/xmark-solid-full.svg"

        and: "Button overrides SvgIcon as icon from Action"
        view.overrideFontIconActionButton.icon instanceof FontIcon
        ComponentTestUtils.isSameFontIcon(view.overrideFontIconActionButton.icon as FontIcon,
                "lumo-icons", "")

        and: "Button overrides Image as icon from Action"
        view.overrideImageActionButton.icon instanceof Image
        def image = view.overrideImageActionButton.icon as Image
        image.src == "public/images/logo.png"
    }
}

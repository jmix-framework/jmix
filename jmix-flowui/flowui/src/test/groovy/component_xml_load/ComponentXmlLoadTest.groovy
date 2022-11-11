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

package component_xml_load

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.progressbar.ProgressBarVariant
import component_xml_load.screen.ComponentView
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem
import io.jmix.flowui.kit.component.dropdownbutton.ComponentItem
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonVariant
import io.jmix.flowui.kit.component.dropdownbutton.TextItem
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ComponentXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")
    }

    def "Load avatar component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "Avatar attributes will be loaded"
        verifyAll(componentView.avatarId) {
            id.get() == "avatarId"
            abbreviation == "abbreviationString"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            colorIndex == 50
            height == "50px"
            image == "imageString"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            name == "nameString"
            themeName == "large"
            visible
            width == "100px"
        }
    }

    def "Load button component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "Button attributes will be loaded"
        verifyAll(componentView.buttonId) {
            id.get() == "buttonId"
            autofocus
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            icon.element.getAttribute("icon") ==
                    VaadinIcon.YOUTUBE.create().element.getAttribute("icon")
            disableOnClick
            enabled
            height == "50px"
            iconAfterText
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            text == "textString"
            themeNames.containsAll(["large", "primary"])
            title == "buttonTitle"
            visible
            whiteSpace == HasText.WhiteSpace.PRE
            width == "100px"
        }
    }

    def "Load button component with Action from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "Button attributes will be loaded"
        verifyAll(componentView.buttonWithActionId) {
            id.get() == "buttonWithActionId"
            action == componentView.buttonAction
            icon.element.getAttribute("icon")
                    == componentView.buttonAction.icon.element.getAttribute("icon")
            enabled == componentView.buttonAction.enabled
            visible == componentView.buttonAction.visible
            text == componentView.buttonAction.text
            title == componentView.buttonAction.description
            themeNames.containsAll(["primary"])
        }
    }

    def "Load details component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "Details attributes will be loaded"
        verifyAll(componentView.detailsId) {
            id.get() == "detailsId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            enabled
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            summaryText == "summaryTextString"
            themeNames.containsAll(["small", "filled"])
            visible
            width == "100px"
            (getContent().find() as Component).getId().get() == "detailsChild"
        }
    }

    def "Load progressBar component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "ProgressBar attributes will be loaded"
        verifyAll(componentView.progressBarId) {
            id.get() == "progressBarId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            height == "50px"
            !indeterminate
            max == 95
            maxHeight == "55px"
            maxWidth == "120px"
            min == 5
            minHeight == "40px"
            minWidth == "80px"
            themeNames.containsAll([ProgressBarVariant.LUMO_ERROR.getVariantName()])
            value == 67
            visible
            width == "100px"
        }
    }

    def "Load dropdownButton component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "DropdownButton attributes will be loaded"
        verifyAll(componentView.dropdownButtonId) {
            id.get() == "dropdownButtonId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            enabled
            height == "50px"
            icon.element.getAttribute("icon") ==
                    VaadinIcon.GAMEPAD.create().element.getAttribute("icon")
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            openOnHover
            text == "dropdownButtonText"
            themeNames.containsAll([DropdownButtonVariant.LUMO_SMALL.getVariantName(),
                                    DropdownButtonVariant.LUMO_PRIMARY.getVariantName()])
            title == "dropdownButtonTitle"
            visible
            whiteSpace == HasText.WhiteSpace.PRE
            width == "100px"
            getItems().size() == 4
            (getItem("firstActionItem") as ActionItem).getAction().getId() == "action2"
            (getItem("secondActionItem") as ActionItem).getAction().getText() == "Action Text"
            ((getItem("componentItem") as ComponentItem).getContent() as Span).getText() == "content"
            (getItem("textItem") as TextItem).getText() == "textItemContent"
        }
    }

    def "Load comboButton component from XML"() {
        when: "Open the ComponentView"
        def componentView = openScreen(ComponentView.class)

        then: "ComboButton attributes will be loaded"
        verifyAll(componentView.comboButtonId) {
            id.get() == "comboButtonId"
            action.getText() == "Action Text"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            enabled
            height == "50px"
            icon.element.getAttribute("icon") ==
                    VaadinIcon.USER.create().element.getAttribute("icon")
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            openOnHover
            text == "Action Text"
            themeNames.containsAll([DropdownButtonVariant.LUMO_PRIMARY.getVariantName()])
            title == "Action Description"
            visible
            whiteSpace == HasText.WhiteSpace.PRE
            width == "100px"
            getItems().size() == 4
            (getItem("firstActionItem") as ActionItem).getAction().getId() == "action2"
            (getItem("secondActionItem") as ActionItem).getAction().getText() == "Action Text"
            ((getItem("componentItem") as ComponentItem).getContent() as Span).getText() == "content"
            (getItem("textItem") as TextItem).getText() == "textItemContent"
        }
    }
}

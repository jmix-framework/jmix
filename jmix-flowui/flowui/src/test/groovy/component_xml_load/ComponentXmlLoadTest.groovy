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
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.html.Label
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.progressbar.ProgressBarVariant
import com.vaadin.flow.component.shared.Tooltip
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer
import com.vaadin.flow.dom.ElementConstants
import component_xml_load.screen.ComponentView
import io.jmix.flowui.component.upload.receiver.FileTemporaryStorageBuffer
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
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load avatar component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

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

    def "Load icon component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

        then: "Icon component will be loaded"
        verifyAll(componentView.iconId) {
            id.get() == "iconId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            color == "purple"
            getElement().getAttribute("icon") == "vaadin:check"
            getStyle().get(ElementConstants.STYLE_WIDTH) == "2em"
            getStyle().get(ElementConstants.STYLE_HEIGHT) == "2em"
            visible
        }
    }

    def "Load button component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

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
            tabIndex == 3
            themeNames.containsAll(["large", "primary"])
            title == "buttonTitle"
            visible
            whiteSpace == HasText.WhiteSpace.PRE
            width == "100px"


            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load button component with Action from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

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
        def componentView = navigateToView(ComponentView.class)

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

            tooltip.text == "tooltipText"
            tooltip.focusDelay == 1
            tooltip.hideDelay == 2
            tooltip.hoverDelay == 3
            tooltip.manual
            tooltip.opened
            tooltip.position == Tooltip.TooltipPosition.BOTTOM
        }
    }

    def "Load progressBar component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

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
        def componentView = navigateToView(ComponentView.class)

        then: "DropdownButton attributes will be loaded"
        verifyAll(componentView.dropdownButtonId) {
            id.get() == "dropdownButtonId"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            (!dropdownIndicatorVisible)
            enabled
            height == "50px"
            icon.element.getAttribute("icon") ==
                    VaadinIcon.GAMEPAD.create().element.getAttribute("icon")
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            openOnHover
            tabIndex == 3
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
        def componentView = navigateToView(ComponentView.class)

        then: "ComboButton attributes will be loaded"
        verifyAll(componentView.comboButtonId) {
            id.get() == "comboButtonId"
            action.getText() == "Action Text"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            getDropdownIcon().element.getAttribute("icon")
                    == VaadinIcon.CHECK.create().element.getAttribute("icon")
            enabled
            height == "50px"
            icon.element.getAttribute("icon") ==
                    VaadinIcon.USER.create().element.getAttribute("icon")
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            openOnHover
            tabIndex == 3
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

    def "Load upload component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

        then: "Upload attributes will be loaded"
        verifyAll(componentView.uploadId) {
            id.get() == "uploadId"
            acceptedFileTypes.containsAll([".jpg"])

            // CAUTION
            // Vaadin Bug
            // See com.vaadin.flow.component.upload.Upload.isAutoUpload
            //     com.vaadin.flow.component.upload.Upload#setAutoUpload
            // fixed in https://github.com/vaadin/flow/issues/15847
            // waiting for Vaadin 24.0
            !autoUpload

            classNames.containsAll(["cssClassName1", "cssClassName2"])
            dropAllowed
            (dropLabel as Label).getText() == "dropLabelString"
            dropLabelIcon.element.getAttribute("icon") ==
                    VaadinIcon.UPLOAD.create().element.getAttribute("icon")
            height == "50px"
            maxFiles == 5
            maxFileSize == 10480000
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            receiver instanceof MultiFileMemoryBuffer
            (uploadButton as Button).getIcon().element.getAttribute("icon") ==
                    VaadinIcon.UPLOAD_ALT.create().element.getAttribute("icon")
            (uploadButton as Button).getText() == "uploadTextString"
            !visible
            width == "100px"
        }
    }

    def "Load upload component with receiver fqn from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)

        then: "Upload with receiver fqn will be loaded"
        verifyAll(componentView.uploadWithReceiverFqn) {
            id.get() == "uploadWithReceiverFqn"
            receiver instanceof FileTemporaryStorageBuffer
        }
    }
}

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

import com.vaadin.flow.component.HasText
import com.vaadin.flow.component.HtmlComponent
import com.vaadin.flow.component.HtmlContainer
import com.vaadin.flow.component.html.AnchorTarget
import com.vaadin.flow.component.html.IFrame
import com.vaadin.flow.component.html.OrderedList
import com.vaadin.flow.data.value.ValueChangeMode
import component_xml_load.screen.HtmlView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class HtmlComponentXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerScreenBasePackages("component_xml_load.screen")
    }

    def "Load #container from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "#container attributes will be loaded"
        def htmlContainer = htmlView."${container}Id" as HtmlContainer

        verifyAll(htmlContainer) {
            id.get() == "${container}Id"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            enabled
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            title.get() == "${container}Title"
            visible
            whiteSpace == HasText.WhiteSpace.PRE
            width == "100px"
            getElement().getThemeList().containsAll(["badge", "primary", "small"])
            (getChildren().findAny().get() as HtmlContainer).getText() == "${container}Child"
        }

        where:
        container << ["article", "aside", "descriptionList", "term", "description", "div", "emphasis", "footer", "h1",
                      "h2", "h3", "h4", "h5", "h6", "header", "listItem", "p", "pre", "section", "span",
                      "unorderedList", "anchor", "htmlObject", "image", "main", "nav", "orderedList"]
    }

    def "Load specific anchor attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Specific anchor attributes will be loaded"
        def anchor = htmlView.anchorId

        verifyAll(anchor) {
            href == "anchorHref"
            target.get() == AnchorTarget.PARENT.value
        }
    }

    def "Load specific htmlObject attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Specific htmlObject attributes will be loaded"
        def htmlObject = htmlView.htmlObjectId

        verifyAll(htmlObject) {
            data == "data"
            type.get() == "type"
        }
    }

    def "Load specific #container attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Specific #container attributes will be loaded"
        htmlView."${container}Id".ariaLabel.get() == "ariaLabelString"

        where:
        container << ["main", "nav"]
    }

    def "Load specific orderedList attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Specific orderedList attributes will be loaded"
        htmlView.orderedListId.type == OrderedList.NumberingType.UPPERCASE_LETTER
    }

    def "Load #component from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Html component attributes will be loaded"
        def htmlComponent = htmlView."${component}Id" as HtmlComponent

        verifyAll(htmlComponent) {
            id.get() == "${component}Id"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            visible
            width == "100px"
        }

        where:
        component << ["hr", "iframe", "param"]
    }

    def "Load specific hr attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Specific hr attributes will be loaded"
        htmlView.hrId.title.get() == "hrTitle"
    }

    def "Load specific iframe attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Specific iframe attributes will be loaded"
        verifyAll(htmlView.iframeId) {
            allow.get() == "allowString"
            importance.get() == IFrame.ImportanceType.HIGH
            name.get() == "nameString"
            sandbox.get().collect().containsAll([IFrame.SandboxType.ALLOW_SCRIPTS,
                                                 IFrame.SandboxType.ALLOW_TOP_NAVIGATION_BY_USER_ACTIVATION])
            src == "resourceString"
            srcdoc.get() == "resourceDocString"
            title.get() == "iframeTitle"
        }
    }

    def "Load input attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Input attributes will be loaded"
        verifyAll(htmlView.inputId) {
            id.get() == "inputId"
            ariaLabel.get() == "ariaLabelString"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            enabled
            height == "50px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            placeholder.get() == "placeholderString"
            type == "password"
            valueChangeMode == ValueChangeMode.ON_CHANGE
            valueChangeTimeout == 50
            visible
            width == "100px"
        }
    }

    def "Load specific param attributes from XML"() {
        when: "Open the HtmlView"
        def htmlView = openScreen(HtmlView.class)

        then: "Specific param attributes will be loaded"
        verifyAll(htmlView.paramId) {
            name == "nameString"
            title.get() == "paramTitle"
            value.get() == "paramValueString"
        }
    }
}

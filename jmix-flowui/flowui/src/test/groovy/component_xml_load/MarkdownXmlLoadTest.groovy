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

import component_xml_load.screen.MarkdownView
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class MarkdownXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load markdown attributes from XML"() {
        when: "Open the HtmlView"
        def view = navigateToView(MarkdownView.class)

        then: "Specific iframe attributes will be loaded"
        verifyAll(view.markdown) {
            content == "Content"
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

    def "Load markdown content from XML"() {
        when: "Open the HtmlView"
        def view = navigateToView(MarkdownView.class)

        then: "Specific iframe attributes will be loaded"

        view.markdownContent.content.trim() == "### Content\nText"
    }
}

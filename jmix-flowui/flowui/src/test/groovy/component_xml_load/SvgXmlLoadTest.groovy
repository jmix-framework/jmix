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

import component_xml_load.screen.BrokenSvgView
import component_xml_load.screen.SvgView
import io.jmix.flowui.exception.GuiDevelopmentException
import org.springframework.boot.test.context.SpringBootTest
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class SvgXmlLoadTest extends FlowuiTestSpecification {

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load svg attributes from XML"() {
        when: "Open the SvgView"
        def view = navigateToView(SvgView.class)

        then: "Specific svg attributes will be loaded"
        verifyAll(view.svgContent) {
            className == "className1"
            style.get("color") == "red"
            !visible
        }
    }

    def "Load svg content from XML"() {
        when: "Open the SvgView"
        def view = navigateToView(SvgView.class)

        then: "The inline content is rendered starting from the root svg element"
        def content = view.svgContent.element.getProperty("innerHTML")

        content.startsWith("<svg")
        content.contains("<circle cx=\"8\" cy=\"8\" r=\"4\"/>")
    }

    def "Load svg content from file"() {
        when: "Open the SvgView"
        def view = navigateToView(SvgView.class)

        then: "The file content is rendered"
        def content = view.svgFile.element.getProperty("innerHTML")

        content.startsWith("<svg")
        content.contains("<rect x=\"2\" y=\"2\" width=\"12\" height=\"12\"/>")
    }

    def "Throw exception for svg content that is not SVG markup"() {
        when: "Open the view with a content element holding plain text"
        navigateToView(BrokenSvgView.class)

        then: "The loader reports a development error"
        def exception = thrown(GuiDevelopmentException)
        exception.message.contains("'content' element must contain SVG markup wrapped in CDATA")
    }
}

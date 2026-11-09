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
import io.jmix.core.Metadata
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import test_support.entity.City
import test_support.entity.Zoo
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class MarkdownXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    Metadata metadata

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    def "Load markdown attributes from XML"() {
        when: "Open the MarkdownView"
        def view = navigateToView(MarkdownView.class)

        then: "Markdown attributes are loaded"
        verifyAll(view.markdown) {
            content == "Content"
            lineBreaks
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
        when: "Open the MarkdownView"
        def view = navigateToView(MarkdownView.class)

        then: "Markdown attributes are loaded"

        view.markdownContent.content.trim() == "### Content\nText"
    }

    def "Bind markdown content to entity attributes from XML"() {
        def view = navigateToView(MarkdownView)
        def city = metadata.create(City)
        city.name = "City"
        def zoo = metadata.create(Zoo)
        zoo.name = "## Zoo"
        zoo.address = "**Street 1**"
        zoo.city = city

        when: "Set the current entity after binding an empty container"
        view.zooDc.item = zoo

        then: "Content comes from direct, nested and inherited bindings"
        view.boundMarkdown.content == "## Zoo"
        view.cityMarkdown.content == "City"
        view.formMarkdown.content == "**Street 1**"

        when: "Change the bound attribute"
        zoo.name = "## Updated zoo"

        then:
        view.boundMarkdown.content == "## Updated zoo"

        when: "Clear the bound attribute"
        zoo.name = null

        then:
        view.boundMarkdown.content == ""

        when: "Replace the current entity"
        def otherZoo = metadata.create(Zoo)
        otherZoo.name = "Other zoo"
        view.zooDc.item = otherZoo

        then:
        view.boundMarkdown.content == "Other zoo"
        view.cityMarkdown.content == ""
        view.formMarkdown.content == ""

        when: "Clear the container"
        view.zooDc.item = null

        then:
        view.boundMarkdown.content == ""
    }

    def "Bind markdown to the current collection item"() {
        def view = navigateToView(MarkdownView)
        def first = metadata.create(Zoo)
        first.name = "First zoo"
        def second = metadata.create(Zoo)
        second.name = "Second zoo"
        view.zoosDc.items = [first, second]

        when:
        view.zoosDc.item = first

        then:
        view.collectionMarkdown.content == "First zoo"

        when: "Another collection item changes"
        second.name = "Updated second zoo"

        then: "Content still comes from the current item"
        view.collectionMarkdown.content == "First zoo"

        when:
        view.zoosDc.item = second

        then:
        view.collectionMarkdown.content == "Updated second zoo"
    }
}

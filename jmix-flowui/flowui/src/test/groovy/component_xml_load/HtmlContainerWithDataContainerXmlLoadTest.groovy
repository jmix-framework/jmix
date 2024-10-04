/*
 * Copyright 2024 Haulmont.
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

import com.vaadin.flow.component.HtmlContainer
import component_xml_load.screen.HtmlDataContainerView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.Foo
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class HtmlContainerWithDataContainerXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    DataManager dataManager

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def foo = dataManager.create Foo
        foo.name = "Foo"

        dataManager.save foo
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_FOO")
    }

    def "Load #container with DataContainer from XML"() {
        when: "Open the HtmlView"
        def htmlView = navigateToView HtmlDataContainerView

        then: "#container dataContainer attributes will be loaded and data will be bound"
        def htmlContainer = htmlView."${container}Id" as HtmlContainer

        htmlContainer.text == "Foo"

        where:
        container << ["article", "aside", "descriptionList", "term", "description", "div", "emphasis", "footer", "h1",
                      "h2", "h3", "h4", "h5", "h6", "header", "listItem", "p", "pre", "section", "span",
                      "unorderedList", "anchor", "htmlObject", "label", "main", "nav", "orderedList"]
    }
}

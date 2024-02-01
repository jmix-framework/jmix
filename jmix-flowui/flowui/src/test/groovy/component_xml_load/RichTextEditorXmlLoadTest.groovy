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


import com.vaadin.flow.data.value.ValueChangeMode
import component_xml_load.screen.ComponentView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class RichTextEditorXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        def order = dataManager.create(Order)
        order.description = "<strong>Strong</strong> text"

        dataManager.save(order)
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load RichTextEditor component from XML"() {
        when: "Open the ComponentView"
        def componentView = navigateToView(ComponentView.class)
        componentView.loadData()

        then: "RichTextEditor attributes will be loaded"
        verifyAll(componentView.richTextEditor) {
            id.get() == "richTextEditor"
            height == "50px"
            width == "100px"
            maxHeight == "55px"
            maxWidth == "120px"
            minHeight == "40px"
            minWidth == "80px"
            classNames.containsAll(["cssClassName1", "cssClassName2"])
            style.get("color") == "red"
            enabled
            readOnly
            label == "labelString"
            helperText == "helperTextString"
            valueChangeMode == ValueChangeMode.TIMEOUT
            ariaLabel.orElse(null) == "ariaLabelString"
            themeNames.containsAll(["compact", "no-border"])
            !visible

            value == dataManager.load(Order).all().one().description
        }
    }
}

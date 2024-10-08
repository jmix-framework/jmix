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


import com.vaadin.flow.data.renderer.ComponentRenderer
import component_xml_load.screen.FragmentRendererView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class FragmentRendererXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages "component_xml_load.screen"

        def customer = dataManager.create Customer
        customer.name = "test customer"

        dataManager.save customer
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute "delete from TEST_CUSTOMER"
    }

    def "Load fragment item renderer for #component from XML"() {
        when: "Open the FragmentRendererView"
        def fragmentRendererView = navigateToView FragmentRendererView

        then: "Fragment renderer for #component will be loaded"
        fragmentRendererView."${component}Id".itemRenderer instanceof ComponentRenderer

        where:
        component << ["checkboxGroup", "radioButtonGroup", "select", "listBox", "multiSelectListBox"]
    }

    def "Load fragment renderer for dataGridColumn from XML"() {
        when: "Open the FragmentRendererView"
        def fragmentRendererView = navigateToView FragmentRendererView

        then: "Fragment renderer for #component will be loaded"
        fragmentRendererView.dataGridColumnId.renderer instanceof ComponentRenderer
    }
}

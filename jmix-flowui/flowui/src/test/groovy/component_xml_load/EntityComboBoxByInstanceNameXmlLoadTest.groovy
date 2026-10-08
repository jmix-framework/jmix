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

import com.vaadin.flow.data.provider.BackEndDataProvider
import com.vaadin.flow.data.provider.Query
import component_xml_load.screen.LfProductByInstanceNameView
import io.jmix.core.DataManager
import io.jmix.core.security.SystemAuthenticator
import io.jmix.flowui.component.combobox.EntityComboBox
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.lookup_field.LfProduct
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class EntityComboBoxByInstanceNameXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    SystemAuthenticator systemAuthenticator

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_LF_PRODUCT")
    }

    def "byInstanceName itemsQuery loaded from XML produces lazy callback that filters by instance name"() {
        setup:
        systemAuthenticator.runWithSystem {
            ['Apple', 'Apricot', 'Banana'].each { name ->
                def p = dataManager.create(LfProduct)
                p.name = name
                dataManager.save(p)
            }
        }

        when: "Open the view containing an entityComboBox with a byInstanceName itemsQuery"
        def view = navigateToView(LfProductByInstanceNameView)
        def field = view.productField as EntityComboBox<LfProduct>

        then: "the field uses a back-end data provider"
        field.dataProvider instanceof BackEndDataProvider

        when: "fetching with a search string"
        def items = systemAuthenticator.withSystem {
            field.dataProvider.fetch(new Query(0, 10, null, null, 'ap')).toList()
        }

        then: "only items whose instance name matches the search string are returned"
        items*.name == ['Apple', 'Apricot']
    }
}

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

package generation_strategy

import com.vaadin.flow.data.provider.Query
import io.jmix.core.DataManager
import io.jmix.core.security.SystemAuthenticator
import io.jmix.flowui.component.factory.ItemsFetchCallbackSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Customer
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class ItemsFetchCallbackSupportTest extends FlowuiTestSpecification {

    @Autowired
    ItemsFetchCallbackSupport itemsFetchCallbackSupport
    @Autowired
    DataManager dataManager
    @Autowired
    SystemAuthenticator systemAuthenticator
    @Autowired
    JdbcTemplate jdbcTemplate

    void setup() {
        systemAuthenticator.runWithSystem {
            ['Mario', 'Maria', 'Bruno'].each { name ->
                def customer = dataManager.create(Customer)
                customer.name = name
                dataManager.save(customer)
            }
        }
    }

    void cleanup() {
        jdbcTemplate.execute('delete from TEST_CUSTOMER')
    }

    def "entity fetch callback filters and pages"() {
        def callback = itemsFetchCallbackSupport.createEntityFetchCallback(
                Customer,
                "select e from test_Customer e where e.name like :searchString escape '\\' order by e.name",
                '(?i)%${inputString}%',
                true,
                null)

        when: "filtering by substring"
        def items = systemAuthenticator.withSystem {
            callback.fetch(new Query<Customer, String>(0, 10, null, null, 'mar')).toList()
        }

        then:
        items*.name == ['Maria', 'Mario']

        when: "paging is applied"
        def page = systemAuthenticator.withSystem {
            callback.fetch(new Query<Customer, String>(1, 1, null, null, 'mar')).toList()
        }

        then:
        page*.name == ['Mario']
    }
}

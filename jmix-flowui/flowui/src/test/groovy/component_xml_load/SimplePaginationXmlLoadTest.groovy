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

import component_xml_load.screen.SimplePaginationXmlLoadTestView
import io.jmix.core.DataManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import test_support.entity.sales.Order
import test_support.spec.FlowuiTestSpecification

@SpringBootTest
class SimplePaginationXmlLoadTest extends FlowuiTestSpecification {

    @Autowired
    DataManager dataManager

    @Autowired
    JdbcTemplate jdbcTemplate

    @Override
    void setup() {
        registerViewBasePackages("component_xml_load.screen")

        dataManager.save(
                dataManager.create(Order),
                dataManager.create(Order))
    }

    @Override
    void cleanup() {
        jdbcTemplate.execute("delete from TEST_ORDER")
    }

    def "Load SimplePagination from XML"() {
        when:

        def view = navigateToView(SimplePaginationXmlLoadTestView)

        then:

        verifyAll(view.simplePagination) {
            it.id.get() == "simplePagination"
            it.classNames.contains("classNames")
            it.getPaginationLoader() != null
            it.itemsPerPageVisible
            it.itemsPerPageItems == [1, 2, 3, 4]
            it.itemsPerPageDefaultValue == 1
            it.itemsPerPageUnlimitedItemVisible
            !it.visible
        }
    }
}

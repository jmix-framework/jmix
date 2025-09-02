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

import io.jmix.core.DataManager
import io.jmix.core.security.SystemAuthenticator
import org.springframework.beans.factory.annotation.Autowired
import test_support.testmodel.m2m.Product
import test_support.testmodel.m2m.Tag

class EntityLogManyToManyTest extends AbstractEntityLogTest {

    @Autowired
    DataManager dataManager

    @Autowired
    SystemAuthenticator authenticator

    void setup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")

        withTransaction {
            clearTable(em, "AUDIT_ENTITY_LOG")

            saveEntityLogAutoConfFor('test_Product', 'name', 'tags')
        }

        initEntityLogAPI()
    }


    void cleanup() {
        clearTables("AUDIT_LOGGED_ATTR", "AUDIT_LOGGED_ENTITY")
    }

    def "test logging many-to-many attribute"() {
        def tag1 = dataManager.create(Tag)
        tag1.name = 't1'

        def tag2 = dataManager.create(Tag)
        tag2.name = 't2'

        dataManager.save(tag1, tag2)

        when:

        def product = dataManager.create(Product)
        product.name = 'p1'
        product.tags = [tag1]

        authenticator.runWithSystem {
            dataManager.save(product)
        }

        then:

        def entityLogItem = getLatestEntityLogItem('test_Product', product.id)
        loggedValueMatches(entityLogItem, 'name', 'p1')
        loggedValueMatches(entityLogItem, 'tags', '[t1]')

        when:

        def product1 = dataManager.load(Product).id(product.id).one()
        product1.tags = [tag1, tag2]

        authenticator.runWithSystem {
            dataManager.save(product1)
        }

        then:

        def entityLogItem1 = getLatestEntityLogItem('test_Product', product.id)
        loggedValueMatches(entityLogItem1, 'tags', '[t1,t2]')
                || loggedValueMatches(entityLogItem1, 'tags', '[t2,t1]')
        loggedOldValueMatches(entityLogItem1, 'tags', '[t1]')

        when:

        def product2 = dataManager.load(Product).id(product.id).one()
        product2.tags = []

        authenticator.runWithSystem {
            dataManager.save(product2)
        }

        then:

        def entityLogItem2 = getLatestEntityLogItem('test_Product', product.id)
        loggedValueMatches(entityLogItem2, 'tags', '[]')
        loggedOldValueMatches(entityLogItem2, 'tags', '[t1,t2]')
                || loggedOldValueMatches(entityLogItem2, 'tags', '[t2,t1]')

    }
}

/*
 * Copyright (c) 2008-2018 Haulmont.
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

package spec.haulmont.cuba.core.entity_manager

import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.model.not_persistent.CustomerWithNonPersistentRef
import com.haulmont.cuba.core.model.not_persistent.TestNotPersistentEntity
import com.haulmont.cuba.core.model.primary_keys.EntityKey
import com.haulmont.cuba.core.testsupport.TestSupport
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class EntityManagerTest extends CoreTestSpecification {
    @Autowired
    private DataManager dataManager
    @Autowired
    private TestSupport testSupport

    def "non-persistent property from superclass is copied back after merge #1150"() {

        EntityKey entityKey = new EntityKey(tenant: 1, entityId: 10)
        TestNotPersistentEntity notPersistentEntity = new TestNotPersistentEntity(name: 'entity1', info: 'something')
        CustomerWithNonPersistentRef customer = new CustomerWithNonPersistentRef(reason: 'some reason', name: 'cust1', entityKey: entityKey, notPersistentEntity: notPersistentEntity)

        when:

        CustomerWithNonPersistentRef customer1 = dataManager.commit(customer)

        then:

        customer1.name == 'cust1'
        customer1.entityKey.tenant == 1
        customer1.entityKey.entityId == 10
        customer1.notPersistentEntity.name == 'entity1'
        customer1.notPersistentEntity.info == 'something'
        customer1.reason == 'some reason'

        when:

        customer1.name = 'cust11'
        customer1.reason = 'reason11'
        CustomerWithNonPersistentRef customer2 = dataManager.commit(customer1)

        then:

        customer2.name == 'cust11'
        customer2.entityKey.tenant == 1
        customer2.entityKey.entityId == 10
        customer2.notPersistentEntity.name == 'entity1'
        customer2.notPersistentEntity.info == 'something'
        customer2.reason == 'reason11'

        cleanup:

        testSupport.deleteRecord(customer)
    }
}

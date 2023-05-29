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

package spec.haulmont.cuba.core.data_manager

import com.haulmont.cuba.core.Persistence
import com.haulmont.cuba.core.entity.contracts.Id
import com.haulmont.cuba.core.global.AppBeans
import com.haulmont.cuba.core.global.DataManager
import com.haulmont.cuba.core.global.LoadContext
import com.haulmont.cuba.core.global.ValueLoadContext
import com.haulmont.cuba.core.model.common.Group
import com.haulmont.cuba.core.model.common.User
import com.haulmont.cuba.core.model.primary_keys.CompositeKeyEntity
import com.haulmont.cuba.core.model.primary_keys.EntityKey
import com.haulmont.cuba.core.model.sales.OrderLine
import com.haulmont.cuba.core.model.sales.Product
import com.haulmont.cuba.core.testsupport.TestSupport
import groovy.sql.Sql
import io.jmix.core.*
import org.springframework.beans.factory.annotation.Autowired
import spec.haulmont.cuba.core.CoreTestSpecification

class DataManagerTest extends CoreTestSpecification {
    @Autowired
    private DataManager dataManager
    @Autowired
    private Persistence persistence
    @Autowired
    private TestSupport testSupport

    User defaultUser
    Group defaultGroup

    void setup() {
        TestSupport.setAuthenticationToSecurityContext();

        defaultGroup = new Group(name: 'Company')
        defaultUser = new User(login: 'admin', group: defaultGroup)

        dataManager.commit(defaultUser, defaultGroup)
    }

    void cleanup() {
        testSupport.deleteRecord(defaultUser, defaultGroup)
    }


    def "loadList query parameter without implicit conversion"() {
        def group = dataManager.load(LoadContext.create(Group).setId(defaultGroup.id))

        LoadContext.Query query

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "condition by reference object"

        query = LoadContext.createQuery('select u from test$User u where u.group = :group')
        query.setParameter('group', group)
        def users = dataManager.loadList(LoadContext.create(User).setQuery(query).setView('user.browse'))

        then: "ok"

        users.size() > 0

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "condition by reference id"

        query = LoadContext.createQuery('select u from test$User u where u.group.id = :groupId')
        query.setParameter('groupId', group.id)
        users = dataManager.loadList(LoadContext.create(User).setQuery(query).setView('user.browse'))

        then: "ok"

        users.size() > 0

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "wrong condition by reference id"

        query = LoadContext.createQuery('select u from test$User u where u.group.id = :group')
        query.setParameter('group', group)
        users = dataManager.loadList(LoadContext.create(User).setQuery(query).setView('user.browse'))

        then: "fail"

        thrown(IllegalArgumentException)
    }

    def "count query with specified sorting"() {
        when:

        def query = LoadContext.createQuery('select u from test$User u').setSort(Sort.by("login"))

        def count = dataManager.getCount(LoadContext.create(User).setQuery(query).setView('user.browse'))

        then:

        count > 0
    }

    def "loadValues query parameter without implicit conversion"() {
        def group = dataManager.load(LoadContext.create(Group).setId(defaultGroup.id))

        ValueLoadContext.Query query

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "condition by reference object"

        query = ValueLoadContext.createQuery('select u.id, u.login from test$User u where u.group = :group')
        query.setParameter('group', group)
        def list = dataManager.loadValues(new ValueLoadContext().setQuery(query).addProperty('id').addProperty('login'))

        then: "ok"

        list.size() > 0

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "condition by reference id"

        query = ValueLoadContext.createQuery('select u.id, u.login from test$User u where u.group.id = :groupId')
        query.setParameter('groupId', group.id)
        list = dataManager.loadValues(new ValueLoadContext().setQuery(query).addProperty('id').addProperty('login'))

        then: "ok"

        list.size() > 0

        ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

        when: "wrong condition by reference id"

        query = ValueLoadContext.createQuery('select u.id, u.login from test$User u where u.group.id = :group')
        query.setParameter('group', group)
        list = dataManager.loadValues(new ValueLoadContext().setQuery(query).addProperty('id').addProperty('login'))

        then: "fail"

        thrown(IllegalArgumentException)
    }

    def "load without query and id"() {

        when:

        User user = dataManager.load(LoadContext.create(User))

        then:

        noExceptionThrown()
        user != null

        when:

        List<User> users = dataManager.loadList(LoadContext.create(User))

        then:

        noExceptionThrown()
        !users.isEmpty()

        when:

        long count = dataManager.getCount(LoadContext.create(User))

        then:

        noExceptionThrown()
        count > 0
    }

    def "load uses _base view by default"() {

        def product = new Product(name: 'p1', quantity: 100)
        def line = new OrderLine(product: product, quantity: 10)
        dataManager.commit(product, line)

        when:

        def line1 = dataManager.load(Id.of(line)).one()

        then:

        AppBeans.get(EntityStates).isLoadedWithView(line1, FetchPlan.BASE)
        AppBeans.get(MetadataTools).getInstanceName(line1) == 'p1 10'

        cleanup:

        testSupport.deleteRecord(line, product)
    }

    def "load by collection of ids"() {

        def product1 = new Product(name: 'p1', quantity: 100)
        def product2 = new Product(name: 'p2', quantity: 200)
        dataManager.commit(product1, product2)

        when:

        def loadContext = LoadContext.create(Product).setIds([product1.id, product2.id])
        def list = dataManager.loadList(loadContext)

        then:

        list == [product1, product2]

        cleanup:

        testSupport.deleteRecord(product1, product2)
    }

    def "load by collection of ids throws exception if some instance not found"() {

        def product1 = new Product(name: 'p1', quantity: 100)
        dataManager.commit(product1)

        when:

        def loadContext = LoadContext.create(Product).setIds([product1.id, UUID.randomUUID()])
        dataManager.loadList(loadContext)

        then:

        thrown(EntityAccessException)

        cleanup:

        testSupport.deleteRecord(product1)
    }

    def "load by collection of composite ids"() {

        def id1 = new EntityKey(tenant: 1, entityId: 1)
        def id2 = new EntityKey(tenant: 1, entityId: 2)
        def entity1 = new CompositeKeyEntity(id: id1, name: 'e1')
        def entity2 = new CompositeKeyEntity(id: id2, name: 'e2')
        dataManager.commit(entity1, entity2)

        when:

        def loadContext = LoadContext.create(CompositeKeyEntity).setIds([id1, id2])
        def list = dataManager.loadList(loadContext)

        then:

        list == [entity1, entity2]

        cleanup:

        Sql sql = new Sql(persistence.getDataSource())
        sql.execute("delete from TEST_COMPOSITE_KEY where TENANT = $id1.tenant and ENTITY_ID = $id1.entityId")
        sql.execute("delete from TEST_COMPOSITE_KEY where TENANT = $id2.tenant and ENTITY_ID = $id2.entityId")
    }

    def "load by null id"() {

        when:

        Optional<User> optUser = dataManager.load(User).id(null).optional()

        then:

        !optUser.isPresent()

        when:

        dataManager.load(User).id(null).one()

        then:

        thrown(IllegalStateException)
    }

    def "load by empty ids"() {

        when:

        List<User> users = dataManager.load(User).ids([]).list()

        then:

        users.isEmpty()

        when:

        users = dataManager.load(User).ids().list()

        then:

        users.isEmpty()

        when:

        users = dataManager.load(User).ids(null).list()

        then:

        users.isEmpty()
    }

    def "remove"() {
        def product = new Product(name: 'p1', quantity: 100)
        def product1 = dataManager.commit(product)
        product1.quantity = 200
        dataManager.commit(product1)

        when: "cannot remove instance with stale version"
        dataManager.remove(product)

        then:
        thrown(Exception)
        dataManager.load(Id.of(product)).optional().isPresent()

        when: "removing by id always works"
        dataManager.remove(Id.of(product))

        then:
        !dataManager.load(Id.of(product)).optional().isPresent()
    }
}

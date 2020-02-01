/*
 * Copyright 2019 Haulmont.
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

package data_manager

import test_support.entity.TestAppEntity
import test_support.entity.TestCompositeKeyEntity
import test_support.entity.TestEntityKey
import test_support.entity.sales.Product
import test_support.entity.sec.User
import io.jmix.core.*
import test_support.DataSpec

import javax.inject.Inject

class DataManagerTest extends DataSpec {

    @Inject
    DataManager dataManager

    @Inject
    EntityStates entityStates

    def "create commit load"() {
        when:

        def entity = dataManager.create(TestAppEntity)
        entity.name = 'e1'

        then:

        entityStates.isNew(entity)

        when:

        def entity1 = dataManager.commit(entity)

        then:

        entity1.version == 1
        !entityStates.isNew(entity1)
        entityStates.isDetached(entity1)

        when:

        def entity2 = dataManager.load(TestAppEntity).id(entity.id).one()

        then:

        !entityStates.isNew(entity2)
        entityStates.isDetached(entity2)
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
    }

    def "load by collection of ids throws exception if some instance not found"() {

        def product1 = new Product(name: 'p1', quantity: 100)
        dataManager.commit(product1)

        when:

        def loadContext = LoadContext.create(Product).setIds([product1.id, UUID.randomUUID()])
        dataManager.loadList(loadContext)

        then:

        thrown(EntityAccessException)
    }

    def "load by collection of composite ids"() {

        def id1 = new TestEntityKey(tenant: 1, entityId: 1)
        def id2 = new TestEntityKey(tenant: 1, entityId: 2)

        def entity1 = new TestCompositeKeyEntity(id: id1, name: 'e1')
        def entity2 = new TestCompositeKeyEntity(id: id2, name: 'e2')

        dataManager.commit(entity1, entity2)

        when:

        def loadContext = LoadContext.create(TestCompositeKeyEntity).setIds([id1, id2])
        def list = dataManager.loadList(loadContext)

        then:

        list == [entity1, entity2]
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

    def "load key-value entity with sort by property"() {
        setup:

        def entity1 = new TestAppEntity(name: 'entityA')
        def entity2 = new TestAppEntity(name: 'entityB')

        dataManager.commit(entity1, entity2)

        when: "sort by persistent property"

        def context = ValueLoadContext.create()
        context.setProperties(['id', 'name'])
                .setQueryString('select e.id, e.name from test_TestAppEntity e where e.id = :id1 or e.id = :id2')
                .setParameter('id1', entity1.id)
                .setParameter('id2', entity2.id)
                .setSort(Sort.by(Sort.Direction.DESC, 'name'))


        def result = dataManager.loadValues(context)

        then:
        result.size() == 2
        result[0].getValue('id') == entity2.id

        when: "sort by aggregated persistent property"

        context = ValueLoadContext.create()
        context.setProperties(['id', 'min'])
                .setQueryString('select e.id, min(e.name) from test_TestAppEntity e where e.id = :id1 or e.id = :id2 group by e.id')
                .setParameter('id1', entity1.id)
                .setParameter('id2', entity2.id)
                .setSort(Sort.by(Sort.Direction.DESC, 'min'))

        result = dataManager.loadValues(context)

        then:
        result.size() == 2
        result[0].getValue('id') == entity2.id
    }

    def "remove"() {
        def entity1 = new TestAppEntity(name: 'entityA')
        dataManager.commit(entity1)

        when:
        dataManager.remove(Id.of(entity1))

        then:
        !dataManager.load(Id.of(entity1)).optional().isPresent()
    }
}

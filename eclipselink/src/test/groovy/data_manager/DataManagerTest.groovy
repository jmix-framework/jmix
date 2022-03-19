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

import io.jmix.core.*
import io.jmix.core.querycondition.PropertyCondition
import org.springframework.beans.factory.annotation.Autowired
import test_support.DataSpec
import test_support.entity.TestAppEntity
import test_support.entity.TestCompositeKeyEntity
import test_support.entity.TestEntityKey
import test_support.entity.sales.OrderLineA
import test_support.entity.sales.Product
import test_support.entity.sec.User

class DataManagerTest extends DataSpec {

    @Autowired
    DataManager dataManager

    @Autowired
    EntityStates entityStates

    def "create commit load"() {
        when:

        def entity = dataManager.create(TestAppEntity)
        entity.name = 'e1'

        then:

        entityStates.isNew(entity)

        when:

        def entity1 = dataManager.save(entity)

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

        def product1 = dataManager.create(Product)
        product1.name = 'p1'
        product1.quantity = 100

        def product2 = dataManager.create(Product)
        product2.name = 'p2'
        product2.quantity = 200

        dataManager.save(product1, product2)

        when:

        def list = dataManager.load(Product)
                .ids([product1.id, product2.id])
                .list()

        then:

        list == [product1, product2]
    }

    def "load by collection of ids throws exception if some instance not found"() {

        def product1 = dataManager.create(Product)
        product1.name = 'p1'
        product1.quantity = 100

        dataManager.save(product1)

        when:

        dataManager.load(Product)
                .ids([product1.id, UUID.randomUUID()])
                .list()

        then:

        thrown(EntityAccessException)
    }

    def "load by collection of composite ids"() {

        def id1 = new TestEntityKey(tenant: 1, entityId: 1)
        def id2 = new TestEntityKey(tenant: 1, entityId: 2)

        def entity1 = new TestCompositeKeyEntity(id: id1, name: 'e1')
        def entity2 = new TestCompositeKeyEntity(id: id2, name: 'e2')

        dataManager.save(entity1, entity2)

        when:

        def list = dataManager.load(TestCompositeKeyEntity)
                .ids([id1, id2])
                .list()

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

        def entity1 = dataManager.create(TestAppEntity)
        entity1.name = 'entityA'

        def entity2 = dataManager.create(TestAppEntity)
        entity1.name = 'entityB'

        dataManager.save(entity1, entity2)

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
        def entity1 = dataManager.create(TestAppEntity)
        entity1.name = 'entityA'

        dataManager.save(entity1)

        when:
        dataManager.remove(Id.of(entity1))

        then:
        !dataManager.load(Id.of(entity1)).optional().isPresent()
    }

    def "load with sort"() {

        def product1 = dataManager.create(Product)
        product1.name = 'p1'
        product1.quantity = 100

        def product2 = dataManager.create(Product)
        product2.name = 'p2'
        product2.quantity = 200

        def line1 = dataManager.create(OrderLineA)
        line1.product = product1

        def line2 = dataManager.create(OrderLineA)
        line2.product = product2

        dataManager.save(product1, product2, line1, line2)

        when:

        def list = dataManager.load(Product)
                .query("0=0")
                .sort(Sort.by(Sort.Direction.DESC, "name"))
                .list()

        then:

        list == [product2, product1]

        when:

        def list1 = dataManager.load(Product)
                .all()
                .sort(Sort.by(Sort.Direction.DESC, "name"))
                .list()

        then:

        list1 == [product2, product1]

        when:

        def list2 = dataManager.load(OrderLineA)
                .all()
                .sort(Sort.by(Sort.Direction.DESC, "product.name"))
                .list()

        then:

        list2 == [line2, line1]
    }

    def "load by condition"() {

        def product1 = dataManager.create(Product)
        product1.name = 'p1'
        product1.quantity = 100

        def product2 = dataManager.create(Product)
        product2.name = 'p2'
        product2.quantity = 200

        def product3 = dataManager.create(Product)
        product3.name = 'p3'
        product3.quantity = 100

        dataManager.save(product1, product2, product3)

        when:

        def list = dataManager.load(Product)
                .condition(PropertyCondition.equal("quantity", 100))
                .list()

        then:

        list.toSet() == [product1, product3].toSet()

        when:

        def list1 = dataManager.load(Product)
                .query("e.quantity = ?1", 100)
                .condition(PropertyCondition.equal("name", "p1"))
                .list()

        then:

        list1 == [product1]
    }

    def "load by condition with reference"() {

        def product1 = dataManager.create(Product)
        product1.name = 'p1'
        product1.quantity = 100

        def product2 = dataManager.create(Product)
        product2.name = 'p2'
        product2.quantity = 200

        def product3 = dataManager.create(Product)
        product3.name = 'p3'
        product3.quantity = 100

        def line1 = dataManager.create(OrderLineA)
        line1.quantity = 100
        line1.product = product1

        def line2 = dataManager.create(OrderLineA)
        line2.quantity = 200
        line2.product = product2

        def line3 = dataManager.create(OrderLineA)
        line3.quantity = 100
        line3.product = product3

        dataManager.save(product1, product2, product3, line1, line2, line3)

        when:

        def list = dataManager.load(OrderLineA)
                .condition(PropertyCondition.equal("product.quantity", 100))
                .list()

        then:

        list.toSet() == [line1, line3].toSet()

        when:

        def list1 = dataManager.load(OrderLineA)
                .query("e.quantity = ?1", 100)
                .condition(PropertyCondition.equal("product.name", "p1"))
                .list()

        then:

        list1 == [line1]
    }

}

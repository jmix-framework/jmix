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
package events

import io.jmix.core.DataManager
import io.jmix.core.Id
import io.jmix.core.Metadata
import io.jmix.core.Stores
import io.jmix.data.StoreAwareLocator
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Ignore
import test_support.DataSpec
import test_support.entity.events.Bar
import test_support.entity.events.Boo
import test_support.entity.sales.Customer
import test_support.entity.sales.Status

/**
 * @see test_support.listeners.TestLoadSaveEventListener
 */
class EntityLoadSaveEventTest extends DataSpec {

    @Autowired
    private Metadata metadata
    @Autowired
    private DataManager dataManager
    @PersistenceContext
    private EntityManager entityManager
    @Autowired
    private StoreAwareLocator storeAwareLocator

    private Customer customer


    void cleanup() {
        if (customer != null) {
            storeAwareLocator.getJdbcTemplate(Stores.MAIN)
                    .execute("DELETE FROM SALES_CUSTOMER")
        }
    }

    def "EntityLoadingEvent listener populates transient field"() {
        def bar = dataManager.create(Bar)
        bar.name = 'abc'
        bar.amount = 10
        dataManager.save(bar)

        when:
        def bar1 = dataManager.load(Id.of(bar)).one()

        then:
        bar1.description == 'abc:10'
    }

    def "EntityLoading event sent correctly for nested entity"() {
        setup:
        def bar = dataManager.create(Bar)
        bar.name = 'abc'
        bar.amount = 10
        def boo = dataManager.create(Boo)
        boo.bar = bar
        dataManager.save(bar, boo)

        when:
        def loadedBoo = dataManager.load(Id.of(boo))
                .fetchPlan(b -> b.add("bar"))
                .one()
        then:
        loadedBoo.bar.description == 'abc:10'
    }

    def "EntityLoadingEvent is not sent for already managed entity"() {
        setup:
        def bar = dataManager.create(Bar)
        bar.name = 'abcd'
        bar.amount = 101

        def boo = dataManager.create(Boo)
        boo.bar = bar
        dataManager.save(bar, boo)

        when:
        def loadedBoo = storeAwareLocator.getTransactionTemplate(Stores.MAIN)
                .execute { s ->
                    def mergedBar = entityManager.merge(bar)

                    def reloadedBoo = dataManager.load(Id.of(boo))
                            .fetchPlan(b -> b.add("bar"))
                            .one()

                    return reloadedBoo
                }

        then: "EntityLoadingEvent is not sent during merge through EntityManager, managed clone is returned from dataManager"
        loadedBoo.bar.description == null
    }

    def "EntitySavingEvent listener populates persistent fields"() {
        def bar = dataManager.create(Bar)
        bar.description = 'abc:10'
        dataManager.save(bar)

        when:
        def bar1 = dataManager.load(Id.of(bar)).one()

        then:
        bar1.name == 'abc'
        bar1.amount == 10
    }

    def "EntitySavingEvent listener initializes fields"() {
        def bar = dataManager.create(Bar)
        dataManager.save(bar)

        when:
        def bar1 = dataManager.load(Id.of(bar)).one()

        then:
        bar1.name == 'new'
        bar1.amount == 1
    }

    // this test won't pass because in Jmix EntitySavingEvent is sent only for operations through DataManager
    @Ignore
    def "EntitySavingEvent while merge entity"() {
        customer = metadata.create(Customer)
        customer.name = 'customer1'

        when:

        storeAwareLocator.getTransactionTemplate(Stores.MAIN)
                .executeWithoutResult({ s ->
                    entityManager.merge(customer)
                })

        Customer reloadedCustomer = dataManager.load(Customer)
                .id(customer.id)
                .optional()
                .orElse(null)

        then:

        reloadedCustomer.status == Status.OK
    }
}

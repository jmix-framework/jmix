/*
 * Copyright (c) 2008-2017 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package data_stores

import io.jmix.core.Metadata
import io.jmix.core.Stores
import io.jmix.data.StoreAwareLocator
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import test_support.DataSpec
import test_support.TestService
import test_support.entity.cars.Colour
import test_support.entity.multidb.Db1Customer
import test_support.entity.multidb.Db1Order
import test_support.entity.sec.User
import test_support.listeners.TestBeforeCommitTransactionListener

class MultiDbTransactionsTest extends DataSpec {

    @Autowired
    StoreAwareLocator locator
    @Autowired
    TestBeforeCommitTransactionListener listener
    @Autowired
    TestService testService
    @Autowired
    Metadata metadata

    User user
    List entities = []

    def setup() {

        user = metadata.create(User)
        user.login = 'u1'
        user.name = 'user1'

        mainTransaction().executeWithoutResult {
            locator.getEntityManager(Stores.MAIN).persist(this.user)
        }

        listener.before = { managedEntities ->
            entities.clear()
            entities.addAll(managedEntities)
        }
    }

    def cleanup() {
        entities.clear()
        listener.before = null
        locator.getJdbcTemplate(Stores.MAIN).update('delete from CARS_COLOUR')
        locator.getJdbcTemplate(Stores.MAIN).update('delete from SEC_USER')
        locator.getJdbcTemplate('db1').update('delete from ORDER_')
        locator.getJdbcTemplate('db1').update('delete from CUSTOMER')
    }

    private void createColour() {
        def colour = metadata.create(Colour)

        colour.name = "color-" + RandomStringUtils.randomAlphabetic(5)
        colour.description = "some color"

        locator.getEntityManager(Stores.MAIN).persist(colour)
    }

    private void createCustomer() {
        def customer = metadata.create(Db1Customer)
        customer.name = "cust-" + RandomStringUtils.randomAlphabetic(5)

        locator.getEntityManager('db1').persist(customer)
    }

    private void createOrder() {
        Db1Order order = metadata.create(Db1Order)
        order.orderDate = new Date()

        locator.getEntityManager('db1').persist(order)
    }

    def mainTransaction() {
        def template = new TransactionTemplate(locator.getTransactionManager(Stores.MAIN))
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
        return template
    }

    def db1Transaction() {
        def template = new TransactionTemplate(locator.getTransactionManager('db1'))
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW)
        return template
    }

    def "nested tx for different db"() {
        when:
        def main_entities
        def db1_entities

        mainTransaction().executeWithoutResult {
            createColour()
            db1Transaction().executeWithoutResult {
                createCustomer()
            }
            db1_entities = new ArrayList(entities)
        }
        main_entities = new ArrayList(entities)

        then:
        db1_entities.size() == 1
        db1_entities[0] instanceof Db1Customer

        main_entities.size() == 1
        main_entities[0] instanceof Colour
    }

    def "nested tx for different db, work with first after committing nested"() {
        when:
        def main_entities
        def db1_entities

        mainTransaction().executeWithoutResult {
            db1Transaction().executeWithoutResult {
                createCustomer()
            }
            db1_entities = new ArrayList(entities)
            createColour()
        }
        main_entities = new ArrayList(entities)

        then:
        db1_entities.size() == 1
        db1_entities[0] instanceof Db1Customer

        main_entities.size() == 1
        main_entities[0] instanceof Colour
    }

    def "nested tx for different db, work with first before and after committing nested"() {
        when:
        def main_entities
        def db1_entities

        mainTransaction().executeWithoutResult {
            def em = locator.getEntityManager(Stores.MAIN)
            createColour()
            db1Transaction().executeWithoutResult {
                createCustomer()
            }
            db1_entities = new ArrayList(entities)
            def user = em.find(User, user.id)
        }
        main_entities = new ArrayList(entities)

        then:
        db1_entities.size() == 1
        db1_entities[0] instanceof Db1Customer

        main_entities.size() == 2
        main_entities.find { it -> it instanceof Colour } != null
        main_entities.find { it -> it instanceof User } != null
    }

    def "nested tx for different db, deeper nesting"() {
        when:
        def main_entities
        def db1_entities1
        def db1_entities2

        mainTransaction().executeWithoutResult {
            createColour()

            db1Transaction().executeWithoutResult {
                createCustomer()

                db1Transaction().executeWithoutResult {
                    createOrder()
                }
                db1_entities2 = new ArrayList(entities)
            }
            db1_entities1 = new ArrayList(entities)
        }
        main_entities = new ArrayList(entities)

        then:
        db1_entities2.size() == 1
        db1_entities2[0] instanceof Db1Order

        db1_entities1.size() == 1
        db1_entities1[0] instanceof Db1Customer

        main_entities.size() == 1
        main_entities[0] instanceof Colour
    }

    def "nested tx for different db, rollback nested"() {
        when:

        def main_entities
        def db1_entities

        mainTransaction().executeWithoutResult {
            createColour()

            try {
                db1Transaction().executeWithoutResult {
                    createCustomer()

                    throw new RuntimeException()
                }
            } catch (Exception e) {
                // handled somehow
            }
            db1_entities = new ArrayList(entities)
        }
        main_entities = new ArrayList(entities)

        then:
        // rolled back nested
        db1_entities.size() == 0
        // main committed successfully
        main_entities.size() == 1
        main_entities[0] instanceof Colour
    }

    def "can work only with entities from datastore of current tx"() {
        when:
        mainTransaction().executeWithoutResult {
            db1Transaction().executeWithoutResult {
                createColour()
                createCustomer()
            }
        }

        then:
        def e = thrown(IllegalStateException)
        println(e)
    }

    def "nested joined tx for different db"() {
        def mainTx = new TransactionTemplate(locator.getTransactionManager(Stores.MAIN))
        mainTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED)

        when:
        db1Transaction().executeWithoutResult {
            createCustomer()

            mainTx.executeWithoutResult {
                createColour()
            }
        }

        then:
        noExceptionThrown()
    }

    def "declarative tx"() {
        def db1Tx = new TransactionTemplate(locator.getTransactionManager('db1'))
        db1Tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED)

        when:
        testService.createUser()

        db1Tx.executeWithoutResult {
            createCustomer()
        }

        then:
        noExceptionThrown()

        when:

        db1Tx.executeWithoutResult {
            createCustomer()

            testService.createUser()
        }

        then:
        noExceptionThrown()
    }

    def "declarative tx for additional data store"() {
        when:
        def customer = testService.createDb1Customer()

        then:
        noExceptionThrown()
        customer != null
    }
}

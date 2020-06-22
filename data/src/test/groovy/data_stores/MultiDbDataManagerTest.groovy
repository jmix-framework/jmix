/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package data_stores

import io.jmix.core.DataManager
import io.jmix.core.EntitySet
import io.jmix.core.Metadata
import io.jmix.core.impl.DataStoreFactory
import io.jmix.data.StoreAwareLocator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import test_support.DataSpec
import test_support.TestInMemoryDataStore
import test_support.entity.cars.Colour
import test_support.entity.multidb.Db1Customer
import test_support.entity.multidb.Mem1Customer

import javax.persistence.EntityManager
import javax.persistence.PersistenceContext

class MultiDbDataManagerTest extends DataSpec {

    @Autowired
    Metadata metadata
    @Autowired
    DataManager dataManager
    @Autowired
    StoreAwareLocator storeAwareLocator
    @Autowired
    TransactionTemplate transactionTemplate
    @PersistenceContext
    EntityManager entityManager
    @Autowired
    JdbcTemplate jdbcTemplate
    @Autowired
    DataStoreFactory dataStoreFactory

    Colour colour

    def setup() {
        ((TestInMemoryDataStore) dataStoreFactory.get("mem1")).clear()
    }

    def cleanup() {
        try {
            jdbcTemplate.update("delete from CARS_COLOUR")
            storeAwareLocator.getJdbcTemplate("db1").update("delete from CUSTOMER")
        } catch (DataAccessException e) {
            // ignore
        }
    }

    void testOneStore() {
        when:
        Db1Customer customer = metadata.create(Db1Customer.class)
        customer.setId(1L)
        customer.setName("John Doe")
        dataManager.save(customer)

        then:
        storeAwareLocator.getTransactionTemplate("db1").executeWithoutResult {
            EntityManager em = storeAwareLocator.getEntityManager("db1")
            Db1Customer c = em.find(Db1Customer.class, 1L)
            assert c == customer
        }

        and:
        Db1Customer c = dataManager.load(Db1Customer.class).id(1L).one()
        customer == c

        and:
        List<Db1Customer> customers = dataManager.load(Db1Customer.class).list()
        customers.size() == 1
        customers.get(0) == customer
    }

    void testTwoStores() {
        Db1Customer customer = metadata.create(Db1Customer.class)
        customer.setId(1L)
        customer.setName("John Doe")

        colour = metadata.create(Colour.class)
        colour.setName("white-" + colour.getId())
        colour.setDescription("some description")

        when:
        EntitySet saved = dataManager.save(customer, colour)

        then:
        saved.size() == 2

        and:
        storeAwareLocator.getTransactionTemplate("db1").executeWithoutResult {
            EntityManager em = storeAwareLocator.getEntityManager("db1")
            Db1Customer customer1 = em.find(Db1Customer.class, 1L)
            assert customer1 == customer
        }

        and:
        transactionTemplate.executeWithoutResult {
            Colour colour1 = entityManager.find(Colour.class, colour.getId())
            assert colour1 == colour
        }
    }

    void testCustomStore() {
        Mem1Customer customer = metadata.create(Mem1Customer.class)
        customer.setName("John Doe")

        when:
        Mem1Customer committed = dataManager.save(customer)

        then:
        committed == customer

        when:
        Mem1Customer loaded = dataManager.load(Mem1Customer.class).id(customer.getId()).one()

        then:
        loaded == customer

        when:
        List<Mem1Customer> list = dataManager.load(Mem1Customer.class).list()

        then:
        !list.isEmpty()
        list.get(0) == customer

        when:
        dataManager.remove(customer)
        loaded = dataManager.load(Mem1Customer.class).id(customer.getId()).optional().orElse(null)

        then:
        loaded == null
    }
}

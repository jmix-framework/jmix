/*
 * Copyright (c) 2008-2016 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/commercial-software-license for details.
 */

package data_stores

import io.jmix.core.*
import io.jmix.core.impl.DataStoreFactory
import io.jmix.data.StoreAwareLocator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataAccessException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.transaction.support.TransactionTemplate
import test_support.DataSpec
import test_support.TestInMemoryDataStore
import test_support.entity.cars.Colour
import test_support.entity.multidb.*

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
    @Autowired
    FetchPlans fetchPlans

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
        List<Db1Customer> customers = dataManager.load(Db1Customer.class).all().list()
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
        List<Mem1Customer> list = dataManager.load(Mem1Customer.class).all().list()

        then:
        !list.isEmpty()
        list.get(0) == customer

        when:
        dataManager.remove(customer)
        loaded = dataManager.load(Mem1Customer.class).id(customer.getId()).optional().orElse(null)

        then:
        loaded == null
    }

    void testCrossDataStoreReference() {
        when:
        Mem1Customer customer = metadata.create(Mem1Customer)
        customer.setName("John Doe")

        Mem1Customer committedCustomer = dataManager.save(customer)

        Db1Order order = metadata.create(Db1Order.class)
        order.setOrderDate(new Date())
        order.setMem1Customer(committedCustomer)

        Db1Order committedOrder = dataManager.save(order)

        Db1Order loadedOrder = dataManager.load(Db1Order)
                .id(committedOrder.id)
                .fetchPlan({ builder -> builder.add("mem1Customer") })
                .optional().orElse(null)

        then: "entity from another store loaded correctly"
        loadedOrder.mem1Customer != null
    }

    void testNestedCrossDatastoreEntitiesSaving() {
        when:
        Db1Order order = metadata.create(Db1Order)
        Db1Customer customer = metadata.create(Db1Customer)
        order.setCustomer(customer)
        customer.setName("prev")
        dataManager.save(order, customer)

        MainReport report = metadata.create(MainReport)
        report = dataManager.save(report)
        report.setDb1Order(order)
        customer.setName("next")

        def results = dataManager.save(new SaveContext().saving(report, fetchPlans.builder(MainReport)
                .add("db1Order", { b1 ->
                    b1.add("customer", { b2 ->
                        b2.add("name")
                    })
                })
                .build()
        ))

        then: "Nested another store entities saved sucessfully"

        results.get(report).getDb1Order().getCustomer().name == "next"
    }

    void testNestedCrossDatastoreEntitiesCascading() {
        when: "persisting"
        Db1Order order = metadata.create(Db1Order)
        Db1Customer customer = metadata.create(Db1Customer)
        order.setCustomer(customer)
        customer.setName("prev")
        dataManager.save(order, customer)

        MainReport report = metadata.create(MainReport)
        report = dataManager.save(report)
        report.setDb1Order(order)

        ReportHolder holder = metadata.create(ReportHolder)
        dataManager.save(holder)

        holder.setMainReport(report)

        def results = dataManager.save(new SaveContext().saving(holder, fetchPlans.builder(ReportHolder)
                .add("mainReport", { b0 ->
                    b0.add("db1Order", { b1 ->
                        b1.add("customer", { b2 ->
                            b2.add("name")
                        })
                    })
                })
                .build()
        ))

        def reloadedReport = results.get(report)

        then: "cross-datastore nested entities persist cascaded"
        reloadedReport.getDb1Order().getCustomer().name == "prev"


        when: "merging"
        def reloadedHolder = results.get(holder)
        reloadedHolder.getMainReport().getDb1Order().getCustomer().name = "next"

        results = dataManager.save(new SaveContext().saving(reloadedHolder, fetchPlans.builder(ReportHolder)
                .add("mainReport", { b0 ->
                    b0.add("db1Order", { b1 ->
                        b1.add("customer", { b2 ->
                            b2.add("name")
                        })
                    })
                })
                .build()
        ))

        then: "cross-datastore nested entities merge cascaded"
        results.get(reloadedHolder).getMainReport().getDb1Order().getCustomer().name == "next"
    }

}

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

package entity_manager;

import io.jmix.core.*;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.data.DataConfiguration;
import io.jmix.data.PersistenceHints;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.sales.Customer;
import test_support.entity.sales.Order;
import test_support.listeners.TestCustomerListener;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class EntityManagerTest {

    @PersistenceContext
    EntityManager entityManager;

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    @Qualifier("db1JdbcTemplate")
    JdbcTemplate db1Jdbc;

    @Autowired
    TransactionTemplate tx;

    @Autowired
    EntityStates entityStates;

    @Autowired
    FetchPlans fetchPlans;

    @Autowired
    TestCustomerListener customerListener;

    @Autowired
    Metadata metadata;

    List<EntityChangedEvent<Customer>> customerEvents = new ArrayList<>();

    @BeforeEach
    @AfterEach
    public void cleanup() throws Exception {
        customerListener.changedEventConsumer = event -> {
            customerEvents.add(event);
        };
        customerEvents.clear();
        try {
            jdbc.update("delete from SALES_ORDER");
            jdbc.update("delete from SALES_CUSTOMER");
        } catch (DataAccessException e) {
            // ignore
        }
    }

    @Test
    public void testContainerEm() {
        System.out.println(">>> jdbc: " + jdbc.getDataSource().toString());

        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        // when:
        tx.executeWithoutResult(transactionStatus -> {
            entityManager.persist(customer);
        });

        // then:
        List<Map<String, Object>> rows = jdbc.queryForList("select * from SALES_CUSTOMER");
        assertEquals(1, rows.size());
        // and:
        assertEquals(1, customerEvents.size());
    }

    @Disabled
    @Test
    public void testApplicationEm() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        EntityManager em = entityManagerFactory.createEntityManager();
        em.getTransaction().begin();
        em.persist(customer);
        em.getTransaction().commit();
        em.close();

        List<Map<String, Object>> rows = jdbc.queryForList("select * from SALES_CUSTOMER");
        assertEquals(1, rows.size());
    }

    @Test
    public void testFind() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });

        // when:
        Customer customer1 = entityManager.find(Customer.class, customer.getId());

        // then:
        assertEquals(customer, customer1);
        assertTrue(entityStates.isLoaded(customer1, "name"));
        assertTrue(entityStates.isLoaded(customer1, "status"));
    }

    @Test
    public void testFind_Partially() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });

        // when:
        FetchPlan fetchPlan = fetchPlans.builder(Customer.class).add("name").partial().build();

        Customer customer1 = entityManager.find(Customer.class, customer.getId(), PersistenceHints.builder().withFetchPlan(fetchPlan).build());

        // then:
        assertTrue(entityStates.isLoaded(customer1, "name"));
        assertEquals(customer, customer1);
        assertFalse(entityStates.isLoaded(customer1, "status"));
    }

    @Test
    public void testLoadGraph() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        Order order = metadata.create(Order.class);
        order.setCustomer(customer);

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
            entityManager.persist(order);
        });

        // when:
        FetchPlan fetchPlan = fetchPlans.builder(Order.class).add("customer").build();

        Order order1 = tx.execute(status ->
                entityManager.find(Order.class, order.getId(), PersistenceHints.builder().withFetchPlan(fetchPlan).build()));

        // then:
        assertTrue(entityStates.isLoaded(order1, "number"));
        assertTrue(entityStates.isLoaded(order1, "customer"));
        Customer customer1 = order1.getCustomer();
        assertEquals(customer, customer1);
        assertTrue(entityStates.isLoaded(customer1, "name"));
        assertTrue(entityStates.isLoaded(customer1, "status"));
    }

    @Test
    public void testLoadGraph_Partially() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        Order order = metadata.create(Order.class);
        order.setCustomer(customer);

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
            entityManager.persist(order);
        });

        // when:
        FetchPlan fetchPlan = fetchPlans.builder(Order.class).addAll("number", "customer.name").partial().build();

        Order order1 = tx.execute(status ->
                entityManager.find(Order.class, order.getId(), PersistenceHints.builder().withFetchPlan(fetchPlan).build()));

        // then:
        assertTrue(entityStates.isLoaded(order1, "number"));
        assertFalse(entityStates.isLoaded(order1, "date"));
        assertTrue(entityStates.isLoaded(order1, "customer"));
        Customer customer1 = order1.getCustomer();
        assertEquals(customer, customer1);
        assertTrue(entityStates.isLoaded(customer1, "name"));
        assertFalse(entityStates.isLoaded(customer1, "status"));
    }

    @Test
    public void testMerge() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });
        customerEvents.clear();

        // when:
        customer.setName("c11");
        Customer mergedCustomer = tx.execute(status -> {
            return entityManager.merge(customer);
        });

        // then:
        assertEquals(customer, mergedCustomer);
        assertEquals("c11", mergedCustomer.getName());
        assertEquals(customer.getVersion() + 1, (int) mergedCustomer.getVersion());
        // and:
        assertEquals(1, customerEvents.size());
    }

    @Test
    public void testSoftDelete() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });
        customerEvents.clear();

        // when:
        tx.executeWithoutResult(status -> {
            Customer customer1 = entityManager.find(Customer.class, customer.getId());
            entityManager.remove(customer1);
        });

        // then:
        List<Map<String, Object>> rows = jdbc.queryForList("select * from SALES_CUSTOMER");
        assertEquals(1, rows.size());
        assertNotNull(rows.get(0).get("DELETE_TS"));
    }

    @Test
    public void testHardDelete() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });
        customerEvents.clear();

        // when:
        tx.executeWithoutResult(status -> {
            Customer customer1 = entityManager.find(Customer.class, customer.getId());
            entityManager.setProperty(PersistenceHints.SOFT_DELETION, false);
            entityManager.remove(customer1);
        });

        // then:
        List<Map<String, Object>> rows = jdbc.queryForList("select * from SALES_CUSTOMER");
        assertEquals(0, rows.size());
    }

}

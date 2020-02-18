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

import io.jmix.core.EntityStates;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.JmixCoreConfiguration;
import io.jmix.data.JmixDataConfiguration;
import io.jmix.data.OrmProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.JmixDataTestConfiguration;
import test_support.entity.sales.Customer;
import test_support.entity_listener.TestEventListener;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {JmixCoreConfiguration.class, JmixDataConfiguration.class, JmixDataTestConfiguration.class})
public class EntityManagerTest {

    @PersistenceContext
    EntityManager entityManager;

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    TransactionTemplate tx;

    @Autowired
    EntityStates entityStates;

    @Before
    @After
    public void cleanup() throws Exception {
        TestEventListener.customerEvents.clear();
        try {
            jdbc.update("delete from SALES_CUSTOMER");
        } catch (DataAccessException e) {
            // ignore
        }
    }

    @Test
    public void testContainerEm() {
        Customer customer = new Customer();
        customer.setName("c1");

        // when:
        tx.executeWithoutResult(transactionStatus -> {
            entityManager.persist(customer);
        });

        // then:
        List<Map<String, Object>> rows = jdbc.queryForList("select * from SALES_CUSTOMER");
        assertEquals(1, rows.size());
        // and:
        assertEquals(1, TestEventListener.customerEvents.size());
    }

    @Ignore
    @Test
    public void testApplicationEm() {
        Customer customer = new Customer();
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
        Customer customer = new Customer();
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
        Customer customer = new Customer();
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });

        // when:
        FetchPlan fetchPlan = FetchPlanBuilder.of(Customer.class).add("name").build();
        fetchPlan.setLoadPartialEntities(true);

        Customer customer1 = entityManager.find(Customer.class, customer.getId(), OrmProperties.builder().withFetchPlan(fetchPlan).build());

        // then:
        assertTrue(entityStates.isLoaded(customer1, "name"));
        assertEquals(customer, customer1);
        assertFalse(entityStates.isLoaded(customer1, "status"));
    }

    @Test
    public void testMerge() {
        Customer customer = new Customer();
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });
        TestEventListener.customerEvents.clear();

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
        assertEquals(1, TestEventListener.customerEvents.size());
    }

    @Test
    public void testSoftDelete() {
        Customer customer = new Customer();
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });
        TestEventListener.customerEvents.clear();

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
        Customer customer = new Customer();
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });
        TestEventListener.customerEvents.clear();

        // when:
        tx.executeWithoutResult(status -> {
            Customer customer1 = entityManager.find(Customer.class, customer.getId());
            entityManager.setProperty(OrmProperties.SOFT_DELETION, false);
            entityManager.remove(customer1);
        });

        // then:
        List<Map<String, Object>> rows = jdbc.queryForList("select * from SALES_CUSTOMER");
        assertEquals(0, rows.size());
    }

}

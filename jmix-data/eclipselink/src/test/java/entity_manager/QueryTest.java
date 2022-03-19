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

import io.jmix.core.DataManager;
import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.data.PersistenceHints;
import io.jmix.eclipselink.impl.JmixEclipseLinkQuery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.sales.Customer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class QueryTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    DataManager dataManager;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    TransactionTemplate tx;

    @BeforeEach
    @AfterEach
    public void cleanup() throws Exception {
        try {
            jdbc.update("delete from SALES_CUSTOMER");
        } catch (DataAccessException e) {
            // ignore
        }
    }

    @Test
    @Transactional
    public void testUnwrap() {
        // given:
        TypedQuery<Customer> query = entityManager.createQuery("select e from sales_Customer e where e.name = ?1", Customer.class);

        // when:
        JmixEclipseLinkQuery jmixQuery = query.unwrap(JmixEclipseLinkQuery.class);

        // then:
        assertNotNull(jmixQuery);
    }

    @Test
    public void testResultList() {
        // given:
        Customer customer = dataManager.create(Customer.class);
        customer.setName("c1");
        dataManager.save(customer);

        // when:
        List<Customer> customerList = tx.execute(status -> {
            TypedQuery<Customer> query = entityManager.createQuery("select e from sales_Customer e where e.name = ?1", Customer.class);
            return query.setParameter(1, "c1").getResultList();
        });

        // then:
        assertEquals(1, customerList.size());
    }

    @Test
    public void testSoftDelete() {
        Customer customer = dataManager.create(Customer.class);
        customer.setName("c1");

        tx.executeWithoutResult(status -> {
            entityManager.persist(customer);
        });
        tx.executeWithoutResult(status -> {
            Customer customer1 = entityManager.find(Customer.class, customer.getId());
            entityManager.remove(customer1);
        });

        // when:
        List<Customer> list = tx.execute(status -> {
            TypedQuery<Customer> query = entityManager.createQuery("select c from sales_Customer c where c.id = ?1", Customer.class);
            query.setParameter(1, customer.getId());
            return query.getResultList();
        });

        // then:
        assertTrue(list.isEmpty());

        // when:
        list = tx.execute(status -> {
            entityManager.setProperty(PersistenceHints.SOFT_DELETION, false);
            TypedQuery<Customer> query = entityManager.createQuery("select c from sales_Customer c where c.id = ?1", Customer.class);
            query.setParameter(1, customer.getId());
            return query.getResultList();
        });

        // then:
        assertFalse(list.isEmpty());
    }

}

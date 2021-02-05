/*
 * Copyright 2020 Haulmont.
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

package test_support;

import io.jmix.core.*;
import io.jmix.core.impl.DataStoreFactory;
import io.jmix.data.DataConfiguration;
import io.jmix.hibernate.HibernateConfiguration;
import io.jmix.data.StoreAwareLocator;
import io.jmix.hibernate.impl.HibernateDataStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import test_support.entity.Customer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, HibernateConfiguration.class, HibernateTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class HibernateTest {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private DataManager dataManager;

    @Autowired
    private Metadata metadata;

    @Autowired
    private DataStoreFactory dataStoreFactory;

    @Autowired
    private TransactionTemplate tx;

    @Autowired
    private EntityStates entityStates;

    @Autowired
    private StoreAwareLocator storeAwareLocator;

    @BeforeEach
    public void setUp() throws Exception {

    }

    @Test
    public void testDatastore() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        dataStoreFactory.get(HibernateDataStore.STORE_NAME).save(new SaveContext().saving(customer));

        // when:
        Customer customer1 = (Customer) dataStoreFactory.get(HibernateDataStore.STORE_NAME).load(new LoadContext<Customer>(metadata.getClass(Customer.class)).setId(customer.getId()));

        // then:
        assertEquals(customer, customer1);
        assertEquals(customer1.getName(), customer1.getName());
        assertTrue(entityStates.isLoaded(customer1, "name"));
    }

    @Test
    public void testEntityManager() {
        Customer customer = metadata.create(Customer.class);
        customer.setName("c1");

        // when:
        tx.executeWithoutResult(transactionStatus -> {
            entityManager.persist(customer);
        });

//        // then:
        Customer customer1 = entityManager.find(Customer.class, customer.getId());


        assertEquals(customer, customer1);
        assertEquals(customer1.getName(), customer1.getName());
        assertTrue(entityStates.isLoaded(customer1, "name"));
    }
}
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

import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.Stores;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import io.jmix.data.StoreAwareLocator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.DataTestConfiguration;
import test_support.TestContextInititalizer;
import test_support.entity.sales.Customer;
import test_support.entity.sales.Order;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(
        classes = {CoreConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class, DataTestConfiguration.class},
        initializers = {TestContextInititalizer.class}
)
public class NativeQueryTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    JdbcTemplate jdbc;

    @Autowired
    StoreAwareLocator storeAwareLocator;

    @BeforeEach
    @AfterEach
    public void cleanup() throws Exception {
        try {
            jdbc.update("delete from SALES_CUSTOMER");
            jdbc.update("delete from SALES_ORDER");
        } catch (DataAccessException e) {
            // ignore
        }
    }

    @Test
    @Disabled
    public void testNativeQuery() {
        //https://www.cuba-platform.ru/discuss/t/problema-s-podgruzkoj-obekta-po-view-dobavlennogo-v-toj-zhe-tranzakiczii/5123
        // given:
        Customer customer = dataManager.create(Customer.class);
        customer.setName("c1");
        dataManager.save(customer);

        storeAwareLocator.getTransactionTemplate(Stores.MAIN).
                executeWithoutResult(tx -> {
                    EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);

                    entityManager.createNativeQuery("select id from SALES_CUSTOMER where ID = ?1", Customer.class)
                            .setParameter(1, customer.getId())
                            .getSingleResult();

                    Customer reloadedCustomer = (Customer) entityManager.createNativeQuery("select * from SALES_CUSTOMER where ID = ?1", Customer.class)
                            .setParameter(1, customer.getId())
                            .getSingleResult();

                    assertEquals("c1", reloadedCustomer.getName());
                });
    }

    @Test
    @Disabled
    public void testNativeQueryWithDataManager() {
        //https://www.cuba-platform.ru/discuss/t/problema-s-podgruzkoj-obekta-po-view-dobavlennogo-v-toj-zhe-tranzakiczii/5123
        // given:
        Customer customer = dataManager.create(Customer.class);
        customer.setName("c1");

        Order order = dataManager.create(Order.class);
        order.setNumber("c1");
        order.setCustomer(customer);

        dataManager.save(customer, order);

        storeAwareLocator.getTransactionTemplate(Stores.MAIN).
                executeWithoutResult(tx -> {
                    EntityManager entityManager = storeAwareLocator.getEntityManager(Stores.MAIN);

                    entityManager.createNativeQuery("select id from SALES_ORDER where ID = ?1", Order.class)
                            .setParameter(1, order.getId())
                            .getSingleResult();

                    Order reloadedOrder = dataManager.load(Order.class).id(order.getId())
                            .fetchPlan(builder -> {
                                builder.add("number")
                                        .add("customer", FetchPlan.LOCAL);
                            })
                            .one();

                    assertEquals("c1", reloadedOrder.getNumber());
                    assertEquals(customer, reloadedOrder.getCustomer());
                });
    }
}

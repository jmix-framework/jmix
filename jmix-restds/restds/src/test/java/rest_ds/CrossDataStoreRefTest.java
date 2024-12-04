/*
 * Copyright 2024 Haulmont.
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

package rest_ds;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.Customer;
import test_support.entity.Order;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class CrossDataStoreRefTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void testCrossDatastoreReferenceIsNotCleared() {
        Customer customer = createCustomer(null, "new-cust-1-" + now);

        Order order = dataManager.create(Order.class);
        order.setDate(now.toLocalDate());
        order.setNum(RandomStringUtils.randomNumeric(10));
        order.setCustomer(customer);
        dataManager.save(order);

        Order order1 = dataManager.load(Order.class).id(order.getId()).fetchPlan(FetchPlan.BASE).one();
        assertThat(order1.getCustomerId()).isNotNull();

        order1.setNum("new-num");
        dataManager.save(order1);

        Order order2 = dataManager.load(Order.class).id(order.getId()).fetchPlan("order-with-customer").one();
        assertThat(order2.getCustomerId()).isNotNull();
    }

    private Customer createCustomer(String firstName, String lastName) {
        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail("test@mail.com");
        dataManager.save(customer);
        return customer;
    }
}

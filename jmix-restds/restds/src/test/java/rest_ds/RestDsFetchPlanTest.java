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
import io.jmix.core.EntityStates;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AuthenticatedAsSystem;
import test_support.TestRestDsConfiguration;
import test_support.entity.Customer;
import test_support.entity.CustomerRegion;
import test_support.entity.Order;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
public class RestDsFetchPlanTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    EntityStates entityStates;

    @Test
    void testWithoutFetchPlan() {
        Condition condition = LogicalCondition.and(
                PropertyCondition.equal("firstName", "Robert"),
                PropertyCondition.equal("lastName", "Taylor")
        );
        List<Customer> customers = dataManager.load(Customer.class).condition(condition).list();

        assertThat(customers).size().isEqualTo(1);

        Customer customer = customers.get(0);

        assertThat(customer.getRegion()).isNull();
    }

    @Test
    void testWithFetchPlan() {
        Condition condition = LogicalCondition.and(
                PropertyCondition.equal("firstName", "Robert"),
                PropertyCondition.equal("lastName", "Taylor")
        );
        List<Customer> customers = dataManager.load(Customer.class)
                .condition(condition)
                .fetchPlan("customer-with-region")
                .list();

        assertThat(customers).size().isEqualTo(1);

        Customer customer = customers.get(0);

        assertThat(customer.getRegion()).isNotNull();
        assertThat(entityStates.isNew(customer.getRegion())).isFalse();

        customer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-region")
                .one();

        assertThat(customer.getRegion()).isNotNull();
        assertThat(entityStates.isNew(customer.getRegion())).isFalse();
    }

    @Test
    void testOrderCustomerBaseFetchPlan() {
        Order order = dataManager.create(Order.class);
        order.setDate(LocalDate.now());
        order.setNum("111");

        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        order.setCustomer(customer);

        dataManager.save(order, customer);

        Order loadedOrder = dataManager.load(Order.class).id(order.getId()).fetchPlan("order-with-customer").one();

        assertThat(loadedOrder.getCustomer()).isNotNull();
        assertThat(loadedOrder.getCustomer().getLastName()).isEqualTo("Doe");
    }

    @Test
    void testOrderCustomerWithRegionFetchPlan() {
        Order order = dataManager.create(Order.class);
        order.setDate(LocalDate.now());
        order.setNum("111");

        CustomerRegion region = dataManager.create(CustomerRegion.class);
        region.setName("Region 1");
        dataManager.save(region);

        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setRegion(region);

        order.setCustomer(customer);
        dataManager.save(order, customer);

        Order loadedOrder = dataManager.load(Order.class).id(order.getId()).fetchPlan("order-with-customer-and-region").one();

        assertThat(loadedOrder.getCustomer()).isNotNull();
        assertThat(loadedOrder.getCustomer().getRegion()).isNotNull();
        assertThat(loadedOrder.getCustomer().getRegion().getName()).isEqualTo("Region 1");
    }

}

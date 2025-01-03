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

import io.jmix.core.*;
import io.jmix.core.querycondition.Condition;
import io.jmix.core.querycondition.LogicalCondition;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.SampleServiceConnection;
import test_support.entity.Customer;
import test_support.entity.CustomerRegionDto;
import test_support.entity.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class FetchPlanTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    EntityStates entityStates;

    @Autowired
    FetchPlanRepository fetchPlanRepository;

    @Autowired
    FetchPlans fetchPlans;

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
    void testWithInlineFetchPlan() {
        // all
        List<Customer> customers = dataManager.load(Customer.class)
                .all()
                .fetchPlan(fpb ->
                        fpb.addFetchPlan(FetchPlan.BASE).add("region", FetchPlan.BASE)
                )
                .list();

        assertThat(customers).isNotEmpty();

        Customer customer = customers.stream().filter(c -> c.getRegion() != null).findAny().orElseThrow();

        assertThat(entityStates.isNew(customer.getRegion())).isFalse();

        // by conditions
        Condition condition = LogicalCondition.and(
                PropertyCondition.equal("firstName", "Robert"),
                PropertyCondition.equal("lastName", "Taylor")
        );
        customers = dataManager.load(Customer.class)
                .condition(condition)
                .fetchPlan(fpb ->
                        fpb.addFetchPlan(FetchPlan.BASE).add("region", FetchPlan.BASE)
                )
                .list();

        assertThat(customers).size().isEqualTo(1);

        customer = customers.get(0);

        assertThat(customer.getRegion()).isNotNull();
        assertThat(entityStates.isNew(customer.getRegion())).isFalse();

        // by id
        customer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan(fpb ->
                        fpb.addFetchPlan(FetchPlan.BASE).add("region", FetchPlan.BASE)
                )
                .one();

        assertThat(customer.getRegion()).isNotNull();
        assertThat(entityStates.isNew(customer.getRegion())).isFalse();
    }

    @Test
    void testUpdateManyToOneReference() {
        CustomerRegionDto region1 = dataManager.create(CustomerRegionDto.class);
        region1.setName("Region 1");
        dataManager.save(region1);

        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setRegion(region1);

        dataManager.save(customer);

        customer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-region")
                .one();

        assertThat(customer.getRegion()).isEqualTo(region1);

        // change region

        CustomerRegionDto region2 = dataManager.create(CustomerRegionDto.class);
        region2.setName("Region 2");
        dataManager.save(region2);

        customer.setRegion(region2);

        dataManager.save(customer);

        customer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-region")
                .one();

        assertThat(customer.getRegion()).isEqualTo(region2);

        // set region to null

        customer.setRegion(null);

        SaveContext saveContext = new SaveContext()
                .saving(customer, fetchPlanRepository.getFetchPlan(Customer.class, "customer-with-region"));
        dataManager.save(saveContext);

        customer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-region")
                .one();

        assertThat(customer.getRegion()).isNull();
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

        CustomerRegionDto region = dataManager.create(CustomerRegionDto.class);
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

    @Test
    void testCreateUpdateWithInlineFetchPlan() {
        CustomerRegionDto region = dataManager.create(CustomerRegionDto.class);
        region.setName("region-" + LocalDateTime.now());

        Customer customer = dataManager.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");
        customer.setRegion(region);

        SaveContext saveContext = new SaveContext()
                .saving(region)
                .saving(customer, fetchPlans.builder(Customer.class)
                        .addFetchPlan(FetchPlan.BASE)
                        .add("region", FetchPlan.BASE)
                        .build()
                );

        EntitySet saved = dataManager.save(saveContext);
        Customer createdCustomer = saved.get(customer);

        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getLastName()).isEqualTo(newName);
        assertThat(createdCustomer.getEmail()).isEqualTo(customer.getEmail());
        assertThat(createdCustomer.getCreatedBy()).isEqualTo(SampleServiceConnection.CLIENT_ID);
        assertThat(createdCustomer.getCreatedDate()).isNotNull();
        assertThat(createdCustomer.getLastModifiedBy()).isNull();
        assertThat(createdCustomer.getLastModifiedDate()).isNotNull();

        CustomerRegionDto createdRegion = createdCustomer.getRegion();
        assertThat(createdRegion).isNotNull();
        assertThat(createdRegion.getName()).isEqualTo(region.getName());

        createdCustomer.setLastName("updated-cust-" + LocalDateTime.now());

        saveContext = new SaveContext()
                .saving(createdCustomer, fetchPlans.builder(Customer.class)
                        .addFetchPlan(FetchPlan.BASE)
                        .add("region", FetchPlan.BASE)
                        .build()
                );

        saved = dataManager.save(saveContext);

        Customer updatedCustomer = saved.get(createdCustomer);

        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getLastName()).isEqualTo(createdCustomer.getLastName());
        assertThat(updatedCustomer.getEmail()).isEqualTo(createdCustomer.getEmail());
        assertThat(updatedCustomer.getLastModifiedBy()).isEqualTo(SampleServiceConnection.CLIENT_ID);

        CustomerRegionDto updatedRegion = updatedCustomer.getRegion();
        assertThat(updatedRegion).isNotNull();
        assertThat(updatedRegion.getName()).isEqualTo(region.getName());
    }
}

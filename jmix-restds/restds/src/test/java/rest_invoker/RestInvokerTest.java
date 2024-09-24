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

package rest_invoker;

import io.jmix.core.Metadata;
import io.jmix.restds.impl.RestInvoker;
import io.jmix.restds.impl.RestSerialization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RestInvokerTest extends BaseRestDsIntegrationTest {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    Metadata metadata;
    @Autowired
    RestSerialization restSerialization;

    RestInvoker restInvoker;

    @BeforeEach
    void setUp() {
        restInvoker = applicationContext.getBean(RestInvoker.class, "restService1");
    }

    @Test
    void testLoad() {
        var loadListParams = new RestInvoker.LoadListParams("Customer", 0, 0, null, null, null);
        List<Customer> customers = restSerialization.fromJsonCollection(
                restInvoker.loadList(loadListParams),
                Customer.class);

        assertThat(customers).isNotEmpty();

        var loadParams = new RestInvoker.LoadParams("Customer", customers.get(0).getId());
        Customer customer = restSerialization.fromJson(
                restInvoker.load(loadParams),
                Customer.class);

        assertThat(customer).isEqualTo(customers.get(0));
    }

    @Test
    void testCreateUpdateDelete() {
        Customer customer = metadata.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        Customer createdCustomer = restSerialization.fromJson(
                restInvoker.create("Customer", restSerialization.toJson(customer, true)),
                Customer.class);

        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getLastName()).isEqualTo(newName);
        assertThat(createdCustomer.getEmail()).isEqualTo(customer.getEmail());

        createdCustomer.setLastName("updated-cust-" + LocalDateTime.now());

        Customer updatedCustomer = restSerialization.fromJson(
                restInvoker.update("Customer", createdCustomer.getId().toString(), restSerialization.toJson(createdCustomer, true)),
                Customer.class);

        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getLastName()).isEqualTo(createdCustomer.getLastName());
        assertThat(updatedCustomer.getEmail()).isEqualTo(createdCustomer.getEmail());

        restInvoker.delete("Customer", updatedCustomer.getId().toString());

        Customer deletedCustomer = restSerialization.fromJson(
                restInvoker.load(new RestInvoker.LoadParams("Customer", updatedCustomer.getId())),
                Customer.class);

        assertThat(deletedCustomer).isNull();
    }

    @Test
    void testCreate() {
        Customer customer = metadata.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        String entityJson = restSerialization.toJson(customer, true);
        String createdJson = restInvoker.create("Customer", entityJson);

        assertThat(createdJson).isNotNull();

        Customer createdCustomer = restSerialization.fromJson(createdJson, Customer.class);

        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getLastName()).isEqualTo(newName);
        assertThat(createdCustomer.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void testCount() {
        Customer customer = metadata.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        restInvoker.create("Customer", restSerialization.toJson(customer, true));

        long customerCount = restInvoker.count("Customer", null);

        assertThat(customerCount).isGreaterThan(0);

        String filter = """
                {
                    "conditions": [
                        {
                            "property": "id",
                            "operator": "=",
                            "value": "%s"
                        }
                    ]
                }
                """.formatted(customer.getId().toString());

        customerCount = restInvoker.count("Customer", filter);

        assertThat(customerCount).isEqualTo(1);
    }

    @Test
    void testSearch() {
        Customer customer1 = metadata.create(Customer.class);
        String newName1 = "new-cust-1-" + LocalDateTime.now();
        customer1.setLastName(newName1);
        customer1.setEmail("test@mail.com");
        restInvoker.create("Customer", restSerialization.toJson(customer1, true));

        Customer customer2 = metadata.create(Customer.class);
        String newName2 = "new-cust-2-" + LocalDateTime.now();
        customer2.setLastName(newName2);
        customer2.setEmail("test@mail.com");
        restInvoker.create("Customer", restSerialization.toJson(customer2, true));

        String filter = """
                {
                    "conditions": [
                        {
                            "property": "lastName",
                            "operator": "=",
                            "value": "%s"
                        }
                    ]
                }
                """.formatted(newName2);


        var loadListParams = new RestInvoker.LoadListParams("Customer", 0, 0, null, filter, null);
        List<Customer> customers = restSerialization.fromJsonCollection(
                restInvoker.loadList(loadListParams),
                Customer.class);

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer2);
    }
}

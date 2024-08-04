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

package rest_client;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.Metadata;
import io.jmix.restds.RestDsConfiguration;
import io.jmix.restds.impl.GenericRestClient;
import io.jmix.restds.impl.RestConnectionParams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.SampleServiceConnection;
import test_support.TestRestDsConfiguration;
import test_support.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {CoreConfiguration.class, RestDsConfiguration.class, TestRestDsConfiguration.class})
@ExtendWith(SpringExtension.class)
class GenericRestClientTest {

    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    Metadata metadata;

    GenericRestClient client;

    @BeforeEach
    void setUp() {
        client = applicationContext.getBean(
                GenericRestClient.class,
                new RestConnectionParams(SampleServiceConnection.getInstance().getBaseUrl(),
                        SampleServiceConnection.CLIENT_ID,
                        SampleServiceConnection.CLIENT_SECRET));
    }

    @Test
    void testLoad() {
        var loadListParams = new GenericRestClient.LoadListParams("Customer", 0, 0, null, null, null);
        List<Customer> customers = client.loadList(Customer.class, loadListParams);

        assertThat(customers).isNotEmpty();

        var loadParams = new GenericRestClient.LoadParams("Customer", customers.get(0).getId());
        Customer customer = client.load(Customer.class, loadParams);

        assertThat(customer).isEqualTo(customers.get(0));
    }

    @Test
    void testCreateUpdateDelete() {
        Customer customer = metadata.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        Customer createdCustomer = client.create("Customer", customer);

        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getLastName()).isEqualTo(newName);
        assertThat(createdCustomer.getEmail()).isEqualTo(customer.getEmail());

        createdCustomer.setLastName("updated-cust-" + LocalDateTime.now());

        Customer updatedCustomer = client.update("Customer", createdCustomer);

        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getLastName()).isEqualTo(createdCustomer.getLastName());
        assertThat(updatedCustomer.getEmail()).isEqualTo(createdCustomer.getEmail());

        client.delete("Customer", updatedCustomer);

        Customer deletedCustomer = client.load(Customer.class, new GenericRestClient.LoadParams("Customer", updatedCustomer.getId()));

        assertThat(deletedCustomer).isNull();
    }

    @Test
    void testCount() {
        Customer customer = metadata.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        client.create("Customer", customer);

        long customerCount = client.count("Customer", null);

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

        customerCount = client.count("Customer", filter);

        assertThat(customerCount).isEqualTo(1);
    }

    @Test
    void testSearch() {
        Customer customer1 = metadata.create(Customer.class);
        String newName1 = "new-cust-1-" + LocalDateTime.now();
        customer1.setLastName(newName1);
        customer1.setEmail("test@mail.com");
        client.create("Customer", customer1);

        Customer customer2 = metadata.create(Customer.class);
        String newName2 = "new-cust-2-" + LocalDateTime.now();
        customer2.setLastName(newName2);
        customer2.setEmail("test@mail.com");
        client.create("Customer", customer2);

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


        var loadListParams = new GenericRestClient.LoadListParams("Customer", 0, 0, null, filter, null);
        List<Customer> customers = client.loadList(Customer.class, loadListParams);

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer2);
    }
}

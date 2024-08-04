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

import io.jmix.core.CoreConfiguration;
import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.Metadata;
import io.jmix.core.querycondition.PropertyCondition;
import io.jmix.restds.RestDsConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TestRestDsConfiguration;
import test_support.entity.Customer;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = {CoreConfiguration.class, RestDsConfiguration.class, TestRestDsConfiguration.class})
@ExtendWith(SpringExtension.class)
public class RestDataStoreTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;
    
    LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
    }

    @Test
    void testLoad() {
        List<Customer> customers = dataManager.load(Customer.class).all().list();

        assertThat(customers).isNotEmpty();

        Customer customer = dataManager.load(Customer.class).id(customers.get(0).getId()).one();

        assertThat(customer).isEqualTo(customers.get(0));
    }

    @Test
    void testCreateUpdateDelete() {
        Customer customer = dataManager.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        Customer createdCustomer = dataManager.save(customer);

        assertThat(createdCustomer).isNotNull();
        assertThat(createdCustomer.getLastName()).isEqualTo(newName);
        assertThat(createdCustomer.getEmail()).isEqualTo(customer.getEmail());

        createdCustomer.setLastName("updated-cust-" + LocalDateTime.now());

        Customer updatedCustomer = dataManager.save(createdCustomer);

        assertThat(updatedCustomer).isNotNull();
        assertThat(updatedCustomer.getLastName()).isEqualTo(createdCustomer.getLastName());
        assertThat(updatedCustomer.getEmail()).isEqualTo(createdCustomer.getEmail());

        dataManager.remove(updatedCustomer);

        Customer deletedCustomer = dataManager.load(Customer.class).id(updatedCustomer.getId()).optional().orElse(null);

        assertThat(deletedCustomer).isNull();
    }

    @Test
    void testCount() {
        Customer customer = createCustomer(null, "new-cust-1-" + now);

        LoadContext<Object> loadContext = new LoadContext<>(metadata.getClass(Customer.class)).setQuery(
                new LoadContext.Query("")
                        .setCondition(PropertyCondition.equal("id", customer.getId()))
                        .setParameter("id", customer.getId())
        );
        long customerCount = dataManager.getCount(loadContext);

        assertThat(customerCount).isEqualTo(1);
    }

    @Test
    void testCondition() {
        Customer customer1 = createCustomer(null, "testCondition-cust-1-" + now);
        Customer customer2 = createCustomer(null, "testCondition-cust-2-" + now);

        List<Customer> customers = dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("lastName", customer2.getLastName()))
                .list();

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer2);
    }

    @Test
    void testQuery() {
        String firstName = "testQuery-firstName-" + now;
        Customer customer1 = createCustomer(firstName, "testQuery-cust-1-" + now);
        Customer customer2 = createCustomer(firstName, "testQuery-cust-2-" + now);

        String query = """
                {
                  "property": "firstName",
                  "operator": "=",
                  "value": "%s"
                }
                """.formatted(firstName);

        // test query only
        List<Customer> customers = dataManager.load(Customer.class).query(query).list();

        assertThat(customers).size().isEqualTo(2);

        // test query and condition
        customers = dataManager.load(Customer.class)
                .query(query)
                .condition(PropertyCondition.equal("lastName", customer1.getLastName()))
                .list();

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer1);
    }

    @Test
    void testQueryWithParameters() {
        String firstName = "testQuery-firstName-" + now;
        Customer customer1 = createCustomer(firstName, "testQuery-cust-1-" + now);
        Customer customer2 = createCustomer(firstName, "testQuery-cust-2-" + now);

        String query = """
                {
                  "property": "firstName",
                  "operator": "=",
                  "parameterName": "name"
                }
                """;

        List<Customer> customers = dataManager.load(Customer.class)
                .query(query)
                .parameter("name", firstName)
                .list();

        assertThat(customers).size().isEqualTo(2);
    }

    @Test
    void testQueryAndConditionWithParameters() {
        String firstName = "testQuery-firstName-" + now;
        Customer customer1 = createCustomer(firstName, "testQuery-cust-1-" + now);
        Customer customer2 = createCustomer(firstName, "testQuery-cust-2-" + now);

        PropertyCondition condition = PropertyCondition.createWithParameterName("firstName", PropertyCondition.Operation.EQUAL, "name");
        condition.setParameterValue(firstName);

        List<Customer> customers = dataManager.load(Customer.class)
                .condition(condition)
                .list();

        assertThat(customers).size().isEqualTo(2);

        String query = """
                {
                  "property": "lastName",
                  "operator": "startsWith",
                  "parameterName": "last_name"
                }
                """;

        customers = dataManager.load(Customer.class)
                .query(query)
                .parameter("last_name", customer1.getLastName())
                .condition(condition)
                .list();

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer1);
    }

    private Customer createCustomer(String firstName, String lastName) {
        Customer customer = metadata.create(Customer.class);
        customer.setFirstName(firstName);
        customer.setLastName(lastName);
        customer.setEmail("test@mail.com");
        dataManager.save(customer);
        return customer;
    }
}

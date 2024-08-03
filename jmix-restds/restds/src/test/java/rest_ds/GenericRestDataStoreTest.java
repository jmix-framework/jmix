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
public class GenericRestDataStoreTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    Metadata metadata;

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
        Customer customer = dataManager.create(Customer.class);
        String newName = "new-cust-" + LocalDateTime.now();
        customer.setLastName(newName);
        customer.setEmail("test@mail.com");

        dataManager.save(customer);

        LoadContext<Object> loadContext = new LoadContext<>(metadata.getClass(Customer.class)).setQuery(
                new LoadContext.Query("")
                        .setCondition(PropertyCondition.equal("id", customer.getId()))
                        .setParameter("id", customer.getId())
        );
        long customerCount = dataManager.getCount(loadContext);

        assertThat(customerCount).isEqualTo(1);
    }

    @Test
    void testSearch() {
        Customer customer1 = metadata.create(Customer.class);
        String newName1 = "new-cust-1-" + LocalDateTime.now();
        customer1.setLastName(newName1);
        customer1.setEmail("test@mail.com");
        dataManager.save(customer1);

        Customer customer2 = metadata.create(Customer.class);
        String newName2 = "new-cust-2-" + LocalDateTime.now();
        customer2.setLastName(newName2);
        customer2.setEmail("test@mail.com");
        dataManager.save(customer2);

        List<Customer> customers = dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("lastName", newName2))
                .list();

        assertThat(customers).size().isEqualTo(1);
        assertThat(customers.get(0)).isEqualTo(customer2);
    }
}

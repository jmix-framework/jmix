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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AuthenticatedAsSystem;
import test_support.TestRestDsConfiguration;
import test_support.entity.ContactType;
import test_support.entity.Customer;
import test_support.entity.CustomerContact;

import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
public class RestDsCompositionTest {

    @Autowired
    DataManager dataManager;

    @Test
    void testLoadComposition() {
        Customer customer = dataManager.load(Customer.class)
                .id(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .fetchPlan("customer-with-contacts")
                .one();

        assertThat(customer.getContacts()).isNotNull();
        assertThat(customer.getContacts()).hasSize(2);
    }

    @Test
    void testSaveComposition() {
        // create customer and 2 contacts and save

        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Smith");
        customer.setEmail("john@example.com");
        customer.setContacts(new HashSet<>());

        CustomerContact contact1 = dataManager.create(CustomerContact.class);
        contact1.setCustomer(customer);
        contact1.setContactType(ContactType.PHONE);
        contact1.setContactValue("333-333-3333");
        contact1.setPreferred(true);
        customer.getContacts().add(contact1);

        CustomerContact contact2 = dataManager.create(CustomerContact.class);
        contact2.setCustomer(customer);
        contact2.setContactType(ContactType.EMAIL);
        contact2.setContactValue("john@example.com");
        customer.getContacts().add(contact2);

        dataManager.save(customer);

        Customer loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-contacts")
                .one();

        assertThat(loadedCustomer.getContacts()).hasSize(2);

        // change second contact and save

        CustomerContact contact = loadedCustomer.getContacts().stream().filter(c -> c.getId().equals(contact2.getId())).findFirst().get();
        contact.setContactValue("john@111.com");

        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-contacts")
                .one();

        assertThat(loadedCustomer.getContacts()).hasSize(2);
        contact = loadedCustomer.getContacts().stream().filter(c -> c.getId().equals(contact2.getId())).findFirst().get();
        assertThat(contact.getContactValue()).isEqualTo("john@111.com");

        // remove second contact and save

        loadedCustomer.getContacts().remove(contact);

        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-contacts")
                .one();

        assertThat(loadedCustomer.getContacts()).hasSize(1);
        assertThat(loadedCustomer.getContacts().iterator().next().getContactValue()).isEqualTo("333-333-3333");

        // load customer without contacts and save

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .one();

        assertThat(loadedCustomer.getContacts()).isNull();

        loadedCustomer.setEmail("john@111.com");
        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-contacts")
                .one();

        assertThat(loadedCustomer.getContacts()).hasSize(1); // contacts are intact

        // clear contacts and save

        loadedCustomer.getContacts().clear();
        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-contacts")
                .one();

        assertThat(loadedCustomer.getContacts()).hasSize(0);
    }
}

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
import io.jmix.core.security.AccessDeniedException;
import io.jmix.core.security.SystemAuthenticator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TestRestDsConfiguration;
import test_support.entity.ContactType;
import test_support.entity.Customer;
import test_support.entity.CustomerContact;
import test_support.entity.CustomerRegion;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static test_support.TestSupport.UUID_1;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith(SpringExtension.class)
public class RestDataStoreSecurityTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    SystemAuthenticator systemAuthenticator;

    @Test
    void testReadOnly() {
        systemAuthenticator.runWithUser("customerReadOnly", () -> {
            List<Customer> customers = dataManager.load(Customer.class).all().list();

            assertThat(customers).isNotEmpty();

            Customer customer = dataManager.create(Customer.class);
            customer.setFirstName("John");
            customer.setLastName("Smith");
            customer.setEmail("john@example.com");
            try {
                dataManager.save(customer);
                fail("Should not be able to save");
            } catch (Exception e) {
                assertThat(e).isInstanceOf(AccessDeniedException.class);
            }

            customers.get(0).setFirstName("John");
            try {
                dataManager.save(customers.get(0));
                fail("Should not be able to update");
            } catch (Exception e) {
                assertThat(e).isInstanceOf(AccessDeniedException.class);
            }

            try {
                dataManager.remove(customers.get(0));
                fail("Should not be able to remove");
            } catch (Exception e) {
                assertThat(e).isInstanceOf(AccessDeniedException.class);
            }
        });
    }

    @Test
    void testNoAccess() {
        systemAuthenticator.runWithUser("customerReadOnly", () -> {

            // disallowed root entity is null or empty list

            List<CustomerRegion> regions = dataManager.load(CustomerRegion.class).all().list();

            assertThat(regions).isEmpty();

            Optional<CustomerRegion> optionalRegion = dataManager.load(CustomerRegion.class)
                    .id(UUID_1)
                    .optional();

            assertThat(optionalRegion).isEmpty();

            // disallowed reference is loaded

            Customer customer = dataManager.load(Customer.class)
                    .id(UUID_1)
                    .fetchPlan("customer-with-region")
                    .one();

            assertThat(customer).isNotNull();
            assertThat(customer.getRegion()).isNotNull();

            // disallowed collection reference is loaded

            customer = dataManager.load(Customer.class)
                    .id(UUID_1)
                    .fetchPlan("customer-with-contacts")
                    .one();

            assertThat(customer.getContacts()).isNotEmpty();
        });
    }

    @Test
    void testRowLevelRole() {
        Customer createdCustomer = systemAuthenticator.withSystem(() -> {
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

            return customer;
        });

        systemAuthenticator.runWithUser("preferredContactOnly", () -> {
            List<CustomerContact> customerContacts = dataManager.load(CustomerContact.class).all().list();

            assertThat(customerContacts).isNotEmpty();
            assertThat(customerContacts).allMatch(cc -> Boolean.TRUE.equals(cc.getPreferred()));

            Customer customer = dataManager.load(Customer.class)
                    .id(createdCustomer.getId())
                    .fetchPlan("customer-with-contacts")
                    .one();

            assertThat(customer).isNotNull();
            assertThat(customer.getContacts()).hasSize(1);
            assertThat(customer.getContacts().iterator().next().getPreferred()).isTrue();

            dataManager.save(customer);
        });

        systemAuthenticator.runWithSystem(() -> {
            Customer customer = dataManager.load(Customer.class)
                    .id(createdCustomer.getId())
                    .fetchPlan("customer-with-contacts")
                    .one();

            assertThat(customer.getContacts()).hasSize(2);
        });

    }
}

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

import io.jmix.core.Metadata;
import io.jmix.core.SaveContext;
import io.jmix.restds.impl.RestSaveContextProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.TestRestDsConfiguration;
import test_support.entity.ContactType;
import test_support.entity.Customer;
import test_support.entity.CustomerContact;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class})
public class RestSaveContextProcessorTest {

    @Autowired
    RestSaveContextProcessor processor;

    @Autowired
    Metadata metadata;

    @Test
    void testCreate() {
        Customer customer = metadata.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Smith");
        customer.setEmail("john@example.com");
        customer.setContacts(new HashSet<>());

        CustomerContact contact1 = metadata.create(CustomerContact.class);
        contact1.setContactType(ContactType.PHONE);
        contact1.setContactValue("333-333-3333");
        contact1.setPreferred(true);
        customer.getContacts().add(contact1);

        CustomerContact contact2 = metadata.create(CustomerContact.class);
        contact2.setContactType(ContactType.EMAIL);
        contact2.setContactValue("john@example.com");
        customer.getContacts().add(contact2);

        SaveContext saveContext = new SaveContext().saving(customer, contact1);

        processor.normalizeCompositionItems(saveContext);

        assertThat(saveContext.getEntitiesToSave()).hasSize(1);
        assertThat(saveContext.getEntitiesToSave()).first().isSameAs(customer);

        assertThat(contact1.getCustomer()).isSameAs(customer);
        assertThat(contact2.getCustomer()).isSameAs(customer);
    }

    @Test
    void testRemoveItem() {
        Customer customer = metadata.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Smith");
        customer.setEmail("john@example.com");
        customer.setContacts(new HashSet<>());

        CustomerContact contact1 = metadata.create(CustomerContact.class);
        contact1.setContactType(ContactType.PHONE);
        contact1.setContactValue("333-333-3333");
        contact1.setPreferred(true);
        customer.getContacts().add(contact1);

        CustomerContact contact2 = metadata.create(CustomerContact.class);
        contact2.setContactType(ContactType.EMAIL);
        contact2.setContactValue("john@example.com");

        SaveContext saveContext = new SaveContext().saving(customer, contact1).removing(contact2);

        processor.normalizeCompositionItems(saveContext);

        assertThat(saveContext.getEntitiesToSave()).hasSize(1);
        assertThat(saveContext.getEntitiesToSave()).first().isEqualTo(customer);
        assertThat(saveContext.getEntitiesToRemove()).isEmpty();
    }
}

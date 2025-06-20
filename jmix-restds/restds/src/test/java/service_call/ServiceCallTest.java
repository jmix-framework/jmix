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

package service_call;

import io.jmix.core.DataManager;
import io.jmix.core.EntitySerialization;
import io.jmix.restds.util.RestDataStoreUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.ContactType;
import test_support.entity.Customer;
import test_support.entity.CustomerContact;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;

public class ServiceCallTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    RestDataStoreUtils restDataStoreUtils;
    @Autowired
    EntitySerialization entitySerialization;

    @Test
    void testServiceCall() {
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

        // test the service call

        RestClient restClient = restDataStoreUtils.getRestClient("restService1");

        customer.setContacts(null);
        String customerJson = entitySerialization.toJson(customer);

        String resultJson = restClient.post()
                .uri("/rest/services/app_Customers/getPreferredContact")
                .body("""
                        {
                            "customer": %s
                        }
                        """.formatted(customerJson))
                .retrieve()
                .body(String.class);

        CustomerContact contact = entitySerialization.entityFromJson(resultJson, null);

        assertThat(contact).isEqualTo(contact1);
    }
}

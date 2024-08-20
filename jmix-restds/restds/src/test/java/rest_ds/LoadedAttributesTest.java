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
import io.jmix.core.FetchPlanRepository;
import io.jmix.core.SaveContext;
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AuthenticatedAsSystem;
import test_support.TestRestDsConfiguration;
import test_support.TestSupport;
import test_support.entity.Customer;
import test_support.entity.CustomerContact;
import test_support.entity.CustomerRegionDto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
public class LoadedAttributesTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    EntityStates entityStates;

    @Autowired
    FetchPlanRepository fetchPlanRepository;

    @Test
    void testLoad() {
        Customer customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).one();

        assertThatLocalAttributesAreLoaded(customer);
        assertThat(entityStates.isLoaded(customer, "region")).isFalse();
        assertThat(entityStates.isLoaded(customer, "contacts")).isFalse();
        assertThat(entityStates.isLoaded(customer, "nonExistingProperty")).isFalse();

        customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).fetchPlan("customer-with-region").one();

        assertThatLocalAttributesAreLoaded(customer);
        assertThat(entityStates.isLoaded(customer, "region")).isTrue();
        assertThat(entityStates.isLoaded(customer, "contacts")).isFalse();

        CustomerRegionDto region = customer.getRegion();

        assertThat(entityStates.isLoaded(region, "id")).isTrue();
        assertThat(entityStates.isLoaded(region, "version")).isTrue();
        assertThat(entityStates.isLoaded(region, "name")).isTrue();

        customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).fetchPlan("customer-with-contacts").one();

        assertThatLocalAttributesAreLoaded(customer);
        assertThat(entityStates.isLoaded(customer, "contacts")).isTrue();

        Set<CustomerContact> contacts = customer.getContacts();
        for (CustomerContact contact : contacts) {
            assertThat(entityStates.isLoaded(contact, "id")).isTrue();
            assertThat(entityStates.isLoaded(contact, "version")).isTrue();
            assertThat(entityStates.isLoaded(contact, "contactType")).isTrue();
            assertThat(entityStates.isLoaded(contact, "contactValue")).isTrue();
            assertThat(entityStates.isLoaded(contact, "preferred")).isTrue();
        }
    }

    @Test
    void testLoadList() {
        List<Customer> customers = dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("id", TestSupport.UUID_1))
                .list();

        Customer customer = customers.get(0);
        assertThatLocalAttributesAreLoaded(customer);
        assertThat(entityStates.isLoaded(customer, "region")).isFalse();
        assertThat(entityStates.isLoaded(customer, "contacts")).isFalse();
        assertThat(entityStates.isLoaded(customer, "nonExistingProperty")).isFalse();
    }

    @Test
    void testCreate() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("cust-" + LocalDateTime.now());

        Customer savedCustomer = dataManager.save(customer);

        assertThatLocalAttributesAreLoaded(savedCustomer);
        assertThat(entityStates.isLoaded(savedCustomer, "region")).isFalse();
        assertThat(entityStates.isLoaded(savedCustomer, "contacts")).isFalse();

        // when reference is set but fetch plan does not include it, the reference is not returned from save
        customer = dataManager.create(Customer.class);
        customer.setLastName("cust-with-region-1-" + LocalDateTime.now());
        customer.setRegion(dataManager.load(CustomerRegionDto.class).id(TestSupport.UUID_1).one());

        savedCustomer = dataManager.save(customer);

        assertThatLocalAttributesAreLoaded(savedCustomer);
        assertThat(entityStates.isLoaded(savedCustomer, "region")).isFalse();

        // when reference is set and fetch plan includes it, the reference is returned from save
        customer = dataManager.create(Customer.class);
        customer.setLastName("cust-with-region-2-" + LocalDateTime.now());
        customer.setRegion(dataManager.load(CustomerRegionDto.class).id(TestSupport.UUID_1).one());

        SaveContext saveContext = new SaveContext()
                .saving(customer, fetchPlanRepository.getFetchPlan(Customer.class, "customer-with-region"));
        savedCustomer = dataManager.save(saveContext).get(customer);

        assertThatLocalAttributesAreLoaded(savedCustomer);
        assertThat(entityStates.isLoaded(savedCustomer, "region")).isTrue();
    }

    @Test
    void testUpdate() {
        Customer customer = dataManager.create(Customer.class);
        customer.setLastName("cust-" + LocalDateTime.now());
        customer = dataManager.save(customer);

        // when saving without fetch plan, reference is not returned from save

        customer.setFirstName("updated");
        customer = dataManager.save(customer);

        assertThatLocalAttributesAreLoaded(customer);
        assertThat(entityStates.isLoaded(customer, "region")).isFalse();

        // when updating the reference without fetch plan, the reference is not returned from save, but it is saved

        CustomerRegionDto region1 = dataManager.load(CustomerRegionDto.class).id(TestSupport.UUID_1).one();
        customer.setRegion(region1);
        customer = dataManager.save(customer);

        assertThatLocalAttributesAreLoaded(customer);
        assertThat(entityStates.isLoaded(customer, "region")).isFalse();

        Customer savedCustomer = getSavedCustomer(customer);
        assertThat(savedCustomer.getRegion()).isEqualTo(region1);

        // when changing reference in a not loaded attribute, the changed reference is saved

        CustomerRegionDto region2 = dataManager.create(CustomerRegionDto.class);
        region2.setName("region-" + LocalDateTime.now());
        dataManager.save(region2);

        customer.setRegion(region2);
        customer = dataManager.save(customer);

        savedCustomer = getSavedCustomer(customer);
        assertThat(savedCustomer.getRegion()).isEqualTo(region2);

        // when saving entity with not loaded reference, it is not removed

        customer = dataManager.load(Customer.class).id(customer.getId()).one();

        customer.setEmail("email-" + LocalDateTime.now());
        dataManager.save(customer);

        savedCustomer = getSavedCustomer(customer);
        assertThat(savedCustomer.getRegion()).isEqualTo(region2);
    }

    private Customer getSavedCustomer(Customer customer) {
        return dataManager.load(Customer.class).id(customer.getId()).fetchPlan("customer-with-region").one();
    }

    private void assertThatLocalAttributesAreLoaded(Customer customer) {
        assertThat(entityStates.isLoaded(customer, "id")).isTrue();
        assertThat(entityStates.isLoaded(customer, "version")).isTrue();
        assertThat(entityStates.isLoaded(customer, "firstName")).isTrue();
        assertThat(entityStates.isLoaded(customer, "lastName")).isTrue();
        assertThat(entityStates.isLoaded(customer, "email")).isTrue();
        assertThat(entityStates.isLoaded(customer, "address")).isTrue();
        assertThat(entityStates.isLoaded(customer.getAddress(), "zip")).isTrue();
        assertThat(entityStates.isLoaded(customer.getAddress(), "addressLine")).isTrue();
    }
}

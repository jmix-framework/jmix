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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.Customer;

import static org.assertj.core.api.Assertions.assertThat;

public class PartialEntityTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;
    @Autowired
    EntityStates entityStates;

    @Test
    void testPartialEntity() {
        var customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        dataManager.save(customer);

        // when loading entity with partial fetch plan, not loaded attributes are null

        var loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-lastName")
                .one();

        assertThat(loadedCustomer.getLastName()).isEqualTo("Doe");
        assertThat(entityStates.isLoaded(loadedCustomer, "firstName")).isFalse();
        assertThat(loadedCustomer.getFirstName()).isNull();

        // when saving entity with partial fetch plan, not loaded attributes are not set to null

        loadedCustomer.setLastName("updated");
        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .one();

        assertThat(loadedCustomer.getLastName()).isEqualTo("updated");
        assertThat(loadedCustomer.getFirstName()).isEqualTo("John");

        // when setting value to not loaded attributes, they are saved

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .fetchPlan("customer-with-lastName")
                .one();

        assertThat(entityStates.isLoaded(loadedCustomer, "firstName")).isFalse();
        loadedCustomer.setFirstName("updated");
        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(Customer.class)
                .id(customer.getId())
                .one();

        assertThat(loadedCustomer.getLastName()).isEqualTo("updated");
        assertThat(loadedCustomer.getFirstName()).isEqualTo("updated");
    }
}

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
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.CustomerWithExtraAttributes;
import test_support.entity.CustomerWithFewerAttributes;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class EntityDifferenceTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Test
    void testDtoWithFewerAttributes() {
        var customer = dataManager.create(CustomerWithFewerAttributes.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        dataManager.save(customer);

        var loadedCustomer = dataManager.load(CustomerWithFewerAttributes.class).id(customer.getId()).one();

        assertThat(loadedCustomer.getFirstName()).isEqualTo("John");
        assertThat(loadedCustomer.getLastName()).isEqualTo("Doe");

        String updatedFirstName = "updated-" + LocalDateTime.now();
        loadedCustomer.setFirstName(updatedFirstName);
        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(CustomerWithFewerAttributes.class).id(customer.getId()).one();
        assertThat(loadedCustomer.getFirstName()).isEqualTo(updatedFirstName);
    }

    @Test
    void testDtoWithExtraAttributes() {
        var customer = dataManager.create(CustomerWithExtraAttributes.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setExtraAttribute("extra");
        dataManager.save(customer);

        var loadedCustomer = dataManager.load(CustomerWithExtraAttributes.class).id(customer.getId()).one();

        assertThat(loadedCustomer.getFirstName()).isEqualTo("John");
        assertThat(loadedCustomer.getLastName()).isEqualTo("Doe");
        assertThat(loadedCustomer.getExtraAttribute()).isNull();

        String updatedFirstName = "updated-" + LocalDateTime.now();
        loadedCustomer.setFirstName(updatedFirstName);
        customer.setExtraAttribute("extra1");
        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(CustomerWithExtraAttributes.class).id(customer.getId()).one();
        assertThat(loadedCustomer.getFirstName()).isEqualTo(updatedFirstName);
        assertThat(loadedCustomer.getExtraAttribute()).isNull();
    }
}

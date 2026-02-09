/*
 * Copyright 2025 Haulmont.
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
import io.jmix.core.FetchPlan;
import io.jmix.core.Id;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.Customer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ElementCollectionTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Test
    void test() {
        // create
        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setPhones(List.of("+111", "+222"));
        dataManager.save(customer);

        // load
        Customer loadedCustomer = loadCustomer(Id.of(customer));
        assertThat(loadedCustomer.getPhones()).containsExactlyInAnyOrder("+111", "+222");

        // update
        loadedCustomer.getPhones().add("+333");
        dataManager.save(loadedCustomer);

        loadedCustomer = loadCustomer(Id.of(customer));
        assertThat(loadedCustomer.getPhones()).containsExactlyInAnyOrder("+111", "+222", "+333");

        // set null
        loadedCustomer.setPhones(null);
        dataManager.save(loadedCustomer);

        loadedCustomer = loadCustomer(Id.of(customer));
        assertThat(loadedCustomer.getPhones()).size().isEqualTo(0);

        // remove
        dataManager.remove(loadedCustomer);
    }

    private @NotNull Customer loadCustomer(Id<Customer> customerId) {
        return dataManager.load(customerId)
                .fetchPlan(fpb -> fpb.addFetchPlan(FetchPlan.BASE).add("phones"))
                .one();
    }
}

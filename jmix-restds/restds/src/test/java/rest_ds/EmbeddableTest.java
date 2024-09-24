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
import io.jmix.core.querycondition.PropertyCondition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import test_support.BaseRestDsIntegrationTest;
import test_support.entity.Customer;
import test_support.entity.CustomerAddress;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EmbeddableTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Test
    void test() {

        // create customer with embedded address

        Customer customer = dataManager.create(Customer.class);
        customer.setFirstName("test");
        customer.setLastName("test");
        customer.setEmail("test@mail.com");

        customer.setAddress(dataManager.create(CustomerAddress.class));
        customer.getAddress().setZip("12345");
        customer.getAddress().setAddressLine("test address");

        dataManager.save(customer);

        // load customer by id

        Customer loadedCustomer = dataManager.load(Customer.class).id(customer.getId()).one();

        CustomerAddress loadedAddress = loadedCustomer.getAddress();
        assertThat(loadedAddress).isNotNull();
        assertThat(loadedAddress.getZip()).isEqualTo(customer.getAddress().getZip());
        assertThat(loadedAddress.getAddressLine()).isEqualTo(customer.getAddress().getAddressLine());

        // load customers by address.zip

        List<Customer> customers = dataManager.load(Customer.class)
                .condition(PropertyCondition.equal("address.zip", "12345"))
                .list();

        assertThat(customers).isNotEmpty();

        // update embedded address

        loadedAddress.setZip("54321");
        loadedAddress.setAddressLine("updated address");

        dataManager.save(loadedCustomer);

        loadedCustomer = dataManager.load(Customer.class).id(loadedCustomer.getId()).one();

        loadedAddress = loadedCustomer.getAddress();
        assertThat(loadedAddress).isNotNull();
        assertThat(loadedAddress.getZip()).isEqualTo("54321");
        assertThat(loadedAddress.getAddressLine()).isEqualTo("updated address");
    }
}

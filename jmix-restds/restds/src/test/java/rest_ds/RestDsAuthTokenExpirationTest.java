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
import io.jmix.core.impl.DataStoreFactory;
import io.jmix.restds.impl.RestDataStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import test_support.AuthenticatedAsSystem;
import test_support.TestRestDsConfiguration;
import test_support.TestSupport;
import test_support.entity.Customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@ContextConfiguration(classes = TestRestDsConfiguration.class)
@ExtendWith({SpringExtension.class, AuthenticatedAsSystem.class})
public class RestDsAuthTokenExpirationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    DataStoreFactory dataStoreFactory;

    @Test
    void test() {
        Customer customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).one();

        assertThat(customer).isNotNull();

        RestDataStore restDataStore = (RestDataStore) dataStoreFactory.get("restService1");
        restDataStore.getRestInvoker().revokeAuthenticationToken();
        // not calling restDataStore.getRestInvoker().resetAuthToken() here to check retry

        try {
            customer = dataManager.load(Customer.class).id(TestSupport.UUID_1).one();
            assertThat(customer).isNotNull();
        } catch (Exception e) {
            fail("Couldn't continue after revoking auth token", e);
        }
    }
}

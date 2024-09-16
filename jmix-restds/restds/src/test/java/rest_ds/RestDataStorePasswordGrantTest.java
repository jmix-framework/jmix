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
import io.jmix.core.security.UserRepository;
import io.jmix.restds.auth.RestAuthenticationToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import test_support.BaseRestDsIntegrationTest;
import test_support.TestSupport;
import test_support.entity.Customer2;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("passwordGrant")
public class RestDataStorePasswordGrantTest extends BaseRestDsIntegrationTest {

    @Autowired
    DataManager dataManager;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        RestAuthenticationToken authenticationToken = new RestAuthenticationToken("admin", "admin");
        authenticationManager.authenticate(authenticationToken);
    }

    @Test
    void testLoad() {
        Customer2 customer = dataManager.load(Customer2.class).id(TestSupport.UUID_1).one();

        assertThat(customer).isNotNull();

        List<Customer2> customers = dataManager.load(Customer2.class).all().list();

        assertThat(customers).isNotEmpty();
    }

    @Test
    void testUserRepository() {
        List<? extends UserDetails> users = userRepository.getByUsernameLike("ad");
        
        assertThat(users).isNotEmpty();
        assertThat(users).anyMatch(user -> "admin".equals(user.getUsername()));
    }
}

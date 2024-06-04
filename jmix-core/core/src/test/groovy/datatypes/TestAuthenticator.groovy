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

package datatypes

import io.jmix.core.CoreConfiguration
import io.jmix.core.Messages
import io.jmix.core.security.InMemoryUserRepository
import io.jmix.core.security.SystemAuthenticator
import io.jmix.core.security.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.User
import org.springframework.test.context.ContextConfiguration
import spock.lang.Specification
import test_support.TestContextInititalizer
import test_support.addon1.TestAddon1Configuration
import test_support.app.TestAppConfiguration
import test_support.base.TestBaseConfiguration

@ContextConfiguration(
        classes = [TestAppConfiguration, TestAddon1Configuration, TestBaseConfiguration, CoreConfiguration],
        initializers = [TestContextInititalizer]
)
class TestAuthenticator extends Specification {
    @Autowired
    Messages messages

    @Autowired
    UserRepository userRepository

    @Autowired
    SystemAuthenticator authenticator

    void createTestUser() {
        if (userRepository instanceof InMemoryUserRepository)  {
            ((InMemoryUserRepository) userRepository).addUser(User.builder()
                    .username("system")
                    .password("")
                    .authorities(Collections.emptyList())
                    .build())
        }

        authenticator.begin()
    }

    void removeTestUser() {
        if (userRepository instanceof InMemoryUserRepository) {
            def system = ((InMemoryUserRepository) userRepository)
                    .loadUserByUsername("system")
            ((InMemoryUserRepository) userRepository).removeUser(system)
        }

        authenticator.end()
    }
}

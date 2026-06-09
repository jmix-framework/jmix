/*
 * Copyright 2026 Haulmont.
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

package password_change_required.test_support;

import io.jmix.core.security.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import test_support.SecurityDataTestConfiguration;
import test_support.repository.TestUserRepository;

/**
 * Test configuration for the {@link io.jmix.securitydata.user.AbstractDatabaseUserRepository} integration test.
 * Imports the common security-data test infrastructure and overrides the default in-memory user repository
 * with {@link TestUserRepository} based on the {@code TEST_USER} table.
 */
@Configuration
@Import(SecurityDataTestConfiguration.class)
public class PasswordChangeRequiredTestConfiguration {

    @Bean("test_userRepository")
    @Primary
    public UserRepository testUserRepository() {
        return new TestUserRepository();
    }
}

/*
 * Copyright 2022 Haulmont.
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

package test_support;

import io.jmix.core.security.CoreSecurityConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import test_support.entity.sec.User;

@EnableWebSecurity
public class UiTestSecurityConfiguration extends CoreSecurityConfiguration {

    public UserRepository userRepository() {
        InMemoryUserRepository repository = new InMemoryUserRepository();
        User user = new User();
        user.setLogin("admin");
        repository.addUser(user);
        return repository;
    }
}

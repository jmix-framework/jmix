/*
 * Copyright 2019 Haulmont.
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

import io.jmix.core.annotation.JmixModule;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.core.security.UserRepository;
import io.jmix.rest.RestConfiguration;
import io.jmix.samples.rest.security.AnonymousAccessRole;
import io.jmix.security.authentication.RoleGrantedAuthority;
import io.jmix.security.role.ResourceRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@Configuration
@JmixModule(dependsOn = RestConfiguration.class)
@PropertySource("classpath:/application.properties")
public class JmixRestTestAnonymousConfiguration {

    @Autowired
    protected ResourceRoleRepository roleRepository;

    @Bean
    @Primary
    public UserRepository userRepository() {
        return new InMemoryUserRepository() {
            @Override
            protected UserDetails createAnonymousUser() {
                return User.builder()
                        .username("anonymous")
                        .password("{noop}")
                        .authorities(RoleGrantedAuthority.ofResourceRole(roleRepository.getRoleByCode(AnonymousAccessRole.NAME)))
                        .build();
            }
        };
    }
}

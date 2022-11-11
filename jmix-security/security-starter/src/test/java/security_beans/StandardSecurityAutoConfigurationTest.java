/*
 * Copyright 2020 Haulmont.
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

package security_beans;

import io.jmix.autoconfigure.core.CoreAutoConfiguration;
import io.jmix.autoconfigure.security.SecurityAutoConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import io.jmix.security.StandardSecurityConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StandardSecurityAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CacheAutoConfiguration.class,
                    CoreAutoConfiguration.class,
                    SecurityAutoConfiguration.class
                    ))
            .withBean(InMemoryUserRepository.class)
            .withAllowBeanDefinitionOverriding(true);

    @Test
    void testCoreSecurityBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("sec_AuthenticationManager");

            Map<String, SecurityFilterChain> beans = context.getBeansOfType(SecurityFilterChain.class);
            assertThat(beans).hasSize(1);
            assertThat(beans.keySet().iterator().next()).isEqualTo("sec_StandardSecurityFilterChain");
        });
    }
}

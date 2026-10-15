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

package h2_console;

import io.jmix.autoconfigure.core.CoreAutoConfiguration;
import io.jmix.autoconfigure.core.cluster.LocalApplicationEventChannelAutoConfiguration;
import io.jmix.autoconfigure.security.SecurityAutoConfiguration;
import io.jmix.core.security.InMemoryUserRepository;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.cache.autoconfigure.CacheAutoConfiguration;
import org.springframework.boot.test.context.assertj.AssertableWebApplicationContext;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import test_support.TestStandardSecurityConfiguration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The dedicated H2 console filter chain. No servlet serves the console here, so a request that passes security
 * ends with 404, while a request rejected by the CSRF protection of the standard chain ends with 403.
 */
class H2ConsoleSecurityFilterChainTest {

    private static final String H2_CONSOLE_CHAIN = "sec_H2ConsoleSecurityFilterChain";

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CacheAutoConfiguration.class,
                    CoreAutoConfiguration.class,
                    LocalApplicationEventChannelAutoConfiguration.class,
                    SecurityAutoConfiguration.class))
            .withUserConfiguration(TestStandardSecurityConfiguration.class)
            .withBean(InMemoryUserRepository.class)
            .withAllowBeanDefinitionOverriding(true);

    @Test
    void h2ConsoleChain_consoleEnabled_postIsNotRejectedByCsrf() {
        contextRunner.withPropertyValues("spring.h2.console.enabled=true").run(context -> {
            assertThat(context).hasBean(H2_CONSOLE_CHAIN);
            mockMvc(context).perform(post("/h2-console/login.do"))
                    .andExpect(status().isNotFound());
        });
    }

    @Test
    void h2ConsoleChain_customPath_coversCustomPathInsteadOfDefault() {
        contextRunner.withPropertyValues("spring.h2.console.enabled=true", "spring.h2.console.path=/db-console")
                .run(context -> {
                    MockMvc mockMvc = mockMvc(context);
                    mockMvc.perform(post("/db-console/login.do")).andExpect(status().isNotFound());
                    mockMvc.perform(post("/h2-console/login.do")).andExpect(status().isForbidden());
                });
    }

    @Test
    void h2ConsoleChain_customPathWithTrailingSlash_coversCustomPath() {
        contextRunner.withPropertyValues("spring.h2.console.enabled=true", "spring.h2.console.path=/db-console/")
                .run(context ->
                        mockMvc(context).perform(post("/db-console/login.do")).andExpect(status().isNotFound()));
    }

    @Test
    void h2ConsoleChain_consoleEnabled_allowsSameOriginFrames() {
        contextRunner.withPropertyValues("spring.h2.console.enabled=true").run(context ->
                mockMvc(context).perform(get("/h2-console/"))
                        .andExpect(header().string("X-Frame-Options", "SAMEORIGIN")));
    }

    @Test
    void h2ConsoleChain_consoleDisabled_chainIsAbsent() {
        contextRunner.run(context ->
                assertThat(context).doesNotHaveBean(H2_CONSOLE_CHAIN));
    }

    @Test
    void h2ConsoleChain_consoleDisabled_postIsRejectedByCsrf() {
        contextRunner.run(context ->
                mockMvc(context).perform(post("/h2-console/login.do"))
                        .andExpect(status().isForbidden()));
    }

    private MockMvc mockMvc(AssertableWebApplicationContext context) {
        return MockMvcBuilders.webAppContextSetup(context.getSourceApplicationContext())
                .addFilters(context.getBean("springSecurityFilterChain", Filter.class))
                .build();
    }
}

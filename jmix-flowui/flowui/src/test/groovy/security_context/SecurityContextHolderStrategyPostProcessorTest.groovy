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

package security_context

import com.vaadin.flow.server.VaadinSession
import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategyConfiguration
import io.jmix.core.security.SecurityContextHelper
import io.jmix.flowui.sys.JmixSecurityContextHolderStrategy
import io.jmix.flowui.sys.SecurityContextHolderStrategyPostProcessor
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.context.SecurityContextHolderStrategy
import org.springframework.security.core.context.SecurityContextImpl
import spock.lang.Specification

/**
 * Runs a context configured as a FlowUI application with Vaadin 24 security: the Vaadin strategy bean comes from
 * {@link VaadinAwareSecurityContextHolderStrategyConfiguration}, as imported by {@code VaadinWebSecurity}.
 */
class SecurityContextHolderStrategyPostProcessorTest extends Specification {

    AnnotationConfigApplicationContext context

    void setup() {
        // Other tests may leave a Vaadin session bound to the thread.
        VaadinSession.setCurrent(null)
    }

    void cleanup() {
        context?.close()
        SecurityContextHolder.clearContext()
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_THREADLOCAL)
    }

    def "test Vaadin strategy bean is replaced with the Jmix strategy and installed into SecurityContextHolder"() {
        when:
        context = new AnnotationConfigApplicationContext(TestSecurityConfiguration)
        SecurityContextHolderStrategy strategy = context.getBean(SecurityContextHolderStrategy)

        then:
        strategy instanceof JmixSecurityContextHolderStrategy
        SecurityContextHolder.getContextHolderStrategy().is(strategy)
    }

    def "test method security uses the context installed for the current thread"() {
        given:
        context = new AnnotationConfigApplicationContext(TestSecurityConfiguration)
        TestSecuredService service = context.getBean(TestSecuredService)
        SecurityContext userContext = new SecurityContextImpl(
                new TestingAuthenticationToken('user', 'pw', 'ROLE_USER'))
        SecurityContext systemContext = new SecurityContextImpl(
                new TestingAuthenticationToken('system', 'pw', 'ROLE_SYSTEM'))
        SecurityContextHolder.setContext(userContext)

        when:
        String username
        SecurityContextHelper.installContext(systemContext)
        try {
            username = service.getSystemUsername()
        } finally {
            SecurityContextHelper.restoreContext(userContext)
        }

        then:
        username == 'system'
        SecurityContextHolder.getContext().is(userContext)
    }

    @Configuration
    @EnableMethodSecurity(proxyTargetClass = true)
    @Import(VaadinAwareSecurityContextHolderStrategyConfiguration)
    static class TestSecurityConfiguration {

        @Bean
        static SecurityContextHolderStrategyPostProcessor securityContextHolderStrategyPostProcessor() {
            return new SecurityContextHolderStrategyPostProcessor()
        }

        @Bean
        TestSecuredService testSecuredService() {
            return new TestSecuredService()
        }
    }

    static class TestSecuredService {

        @PreAuthorize("hasRole('SYSTEM')")
        String getSystemUsername() {
            return SecurityContextHolder.getContext().getAuthentication().getName()
        }
    }
}

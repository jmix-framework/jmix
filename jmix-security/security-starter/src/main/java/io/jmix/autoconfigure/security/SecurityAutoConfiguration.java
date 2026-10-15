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

package io.jmix.autoconfigure.security;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.JmixSecurityFilterChainOrder;
import io.jmix.security.SecurityConfiguration;
import io.jmix.security.SecurityConfigurers;
import io.jmix.security.role.ResourceRoleRepository;
import io.jmix.security.role.RowLevelRoleRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.cache.autoconfigure.JCacheManagerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;

import javax.cache.Cache;
import javax.cache.configuration.MutableConfiguration;

@AutoConfiguration
@Import({CoreConfiguration.class, SecurityConfiguration.class})
public class SecurityAutoConfiguration {


    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer resourceRolesCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(ResourceRoleRepository.RESOURCE_ROLES_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(ResourceRoleRepository.RESOURCE_ROLES_CACHE_NAME, configuration);
            }
        };
    }


    @Bean
    @SuppressWarnings({"rawtypes", "unchecked"})
    JCacheManagerCustomizer rowLevelRolesCacheCustomizer() {
        return cacheManager -> {
            Cache<Object, Object> cache = cacheManager.getCache(RowLevelRoleRepository.ROW_LEVEL_ROLES_CACHE_NAME);
            if (cache == null) {
                MutableConfiguration configuration = new MutableConfiguration();
                cacheManager.createCache(RowLevelRoleRepository.ROW_LEVEL_ROLES_CACHE_NAME, configuration);
            }
        };
    }

    @ConditionalOnMissingBean
    @Bean(name = "sec_PasswordEncoder")
    PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    /**
     * Opens the H2 database console to Spring Security when the console is enabled. The console servlet itself is
     * registered by Spring Boot when {@code spring-boot-h2console} is on the classpath. The console works in frames
     * and does not implement CSRF protection, so its path gets its own filter chain without it. The console is meant
     * for development only.
     */
    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    @ConditionalOnBooleanProperty("spring.h2.console.enabled")
    public static class H2ConsoleSecurityConfiguration {

        public static final String SECURITY_CONFIGURER_QUALIFIER = "h2-console";

        @Bean("sec_H2ConsoleSecurityFilterChain")
        @Order(JmixSecurityFilterChainOrder.H2_CONSOLE)
        SecurityFilterChain h2ConsoleSecurityFilterChain(HttpSecurity http,
                                                         @Value("${spring.h2.console.path:/h2-console}") String path)
                throws Exception {
            String pattern = path.endsWith("/") ? path + "**" : path + "/**";
            http.securityMatcher(PathPatternRequestMatcher.pathPattern(pattern))
                    .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
                    .csrf(CsrfConfigurer::disable)
                    .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin));
            SecurityConfigurers.applySecurityConfigurersWithQualifier(http, SECURITY_CONFIGURER_QUALIFIER);
            return http.build();
        }
    }
}

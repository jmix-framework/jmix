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

package io.jmix.autoconfigure.sessions;

import io.jmix.core.CoreConfiguration;
import io.jmix.sessions.SessionsConfiguration;
import io.jmix.sessions.resolver.OAuth2AndCookieSessionIdResolver;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.HashMap;

@AutoConfiguration
@Import({CoreConfiguration.class, SessionsAutoConfiguration.OAuth2SessionsConfiguration.class,
        SessionsAutoConfiguration.DefaultSessionsConfiguration.class, SessionsConfiguration.class})
@AutoConfigureAfter(SessionAutoConfiguration.class)
public class SessionsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(SessionRepository.class)
    public SessionRepository<MapSession> sessionRepository() {
        return new MapSessionRepository(new HashMap<>());
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(TokenStore.class)
    @ConditionalOnBean(TokenStore.class)
    @Order(10)
    public static class OAuth2SessionsConfiguration {
        @Bean("sess_sessionIdResolver")
        @ConditionalOnMissingBean(HttpSessionIdResolver.class)
        public HttpSessionIdResolver sessionIdResolver() {
            return new OAuth2AndCookieSessionIdResolver();
        }
    }

    @Configuration(proxyBeanMethods = false)
    @Order(20)
    public static class DefaultSessionsConfiguration {
        @Bean("sess_sessionIdResolver")
        @ConditionalOnMissingBean(HttpSessionIdResolver.class)
        public HttpSessionIdResolver sessionIdResolver() {
            return new CookieHttpSessionIdResolver();
        }
    }
}

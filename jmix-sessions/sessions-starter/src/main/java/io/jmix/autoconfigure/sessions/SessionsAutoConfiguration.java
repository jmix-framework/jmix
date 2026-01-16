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
import io.jmix.sessions.SessionsProperties;
import io.jmix.sessions.impl.JmixExpiringSessionMap;
import io.jmix.sessions.resolver.OAuth2AndCookieSessionIdResolver;
import jakarta.servlet.ServletContext;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;


@AutoConfiguration
@Import({CoreConfiguration.class, SessionsAutoConfiguration.OAuth2SessionsConfiguration.class,
        SessionsAutoConfiguration.DefaultSessionsConfiguration.class, SessionsConfiguration.class})
@AutoConfigureAfter(JmixHazelcastSessionsAutoConfiguration.class)
//TODO [SB4] SessionAutoConfiguration.class is gone - should we add some another ordering anchor?
@EnableConfigurationProperties(SessionsProperties.class)
public class SessionsAutoConfiguration {

    @ConditionalOnMissingBean(SessionRepository.class)
    @Configuration(proxyBeanMethods = false)
    public static class SessionRepositoryAutoConfiguration {

        private List<SessionRepositoryCustomizer<MapSessionRepository>> sessionRepositoryCustomizers;

        @Autowired(required = false)
        public void setSessionRepositoryCustomizer(
                ObjectProvider<@NonNull SessionRepositoryCustomizer<MapSessionRepository>> sessionRepositoryCustomizers) {
            this.sessionRepositoryCustomizers = sessionRepositoryCustomizers.orderedStream().collect(Collectors.toList());
        }

        @Bean
        @DependsOn("jmixExpiringSessionMap")
        public SessionRepository<MapSession> sessionRepository(JmixExpiringSessionMap jmixExpiringSessionMap,
                                                               Environment environment,
                                                               ServletContext servletContext) {
            MapSessionRepository mapSessionRepository = new MapSessionRepository(jmixExpiringSessionMap);

            Duration timeout = environment.getProperty("spring.session.timeout", Duration.class); //TODO [SB4]
            if (timeout == null) {
                int minutes = servletContext.getSessionTimeout();
                timeout = Duration.ofMinutes(minutes);
            }

            mapSessionRepository.setDefaultMaxInactiveInterval(timeout);

            this.sessionRepositoryCustomizers
                    .forEach((sessionRepositoryCustomizer) -> sessionRepositoryCustomizer.customize(mapSessionRepository));

            return mapSessionRepository;
        }

        @Bean
        @ConditionalOnMissingBean(JmixExpiringSessionMap.class)
        public JmixExpiringSessionMap jmixExpiringSessionMap(ApplicationEventPublisher applicationEventPublisher,
                                                             SessionsProperties expiringMapProperties) {
            return new JmixExpiringSessionMap(applicationEventPublisher, expiringMapProperties);
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(OAuth2AuthorizationService.class)
    @ConditionalOnBean(OAuth2AuthorizationService.class)
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

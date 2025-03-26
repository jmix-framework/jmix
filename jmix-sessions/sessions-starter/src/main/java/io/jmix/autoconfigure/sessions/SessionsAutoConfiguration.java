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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.session.SessionAutoConfiguration;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.session.MapSession;
import org.springframework.session.MapSessionRepository;
import org.springframework.session.SessionRepository;
import org.springframework.session.config.SessionRepositoryCustomizer;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.util.List;
import java.util.stream.Collectors;


@AutoConfiguration
@Import({CoreConfiguration.class, SessionsAutoConfiguration.OAuth2SessionsConfiguration.class,
        SessionsAutoConfiguration.DefaultSessionsConfiguration.class, SessionsConfiguration.class})
@AutoConfigureAfter({SessionAutoConfiguration.class, JmixHazelcastSessionsAutoConfiguration.class})
@EnableConfigurationProperties(SessionsProperties.class)
public class SessionsAutoConfiguration {

    @ConditionalOnMissingBean(SessionRepository.class)
    @Configuration(proxyBeanMethods = false)
    public static class SessionRepositoryAutoConfiguration {

        private List<SessionRepositoryCustomizer<MapSessionRepository>> sessionRepositoryCustomizers;

        @Autowired(required = false)
        public void setSessionRepositoryCustomizer(
                ObjectProvider<SessionRepositoryCustomizer<MapSessionRepository>> sessionRepositoryCustomizers) {
            this.sessionRepositoryCustomizers = sessionRepositoryCustomizers.orderedStream().collect(Collectors.toList());
        }

        @Bean
        @DependsOn("jmixExpiringSessionMap")
        public SessionRepository<MapSession> sessionRepository(JmixExpiringSessionMap jmixExpiringSessionMap,
                                                               SessionProperties sessionProperties,
                                                               ServerProperties serverProperties) {
            MapSessionRepository mapSessionRepository = new MapSessionRepository(jmixExpiringSessionMap);

            mapSessionRepository.setDefaultMaxInactiveInterval(
                    sessionProperties.determineTimeout(() -> serverProperties.getServlet().getSession().getTimeout()));

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

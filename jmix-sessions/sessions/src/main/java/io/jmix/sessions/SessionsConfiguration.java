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

package io.jmix.sessions;

import io.jmix.core.CoreConfiguration;
import io.jmix.core.annotation.JmixModule;
import io.jmix.core.session.SessionData;
import io.jmix.sessions.resolver.OAuth2AndCookieSessionIdResolver;
import io.jmix.sessions.validators.VaadinSessionAttributesValidator;
import jakarta.servlet.ServletContext;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.http.HttpSessionListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenClaimsContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.session.Session;
import org.springframework.session.SessionRepository;
import org.springframework.session.security.web.authentication.SpringSessionRememberMeServices;
import org.springframework.session.web.http.*;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ComponentScan
@JmixModule(dependsOn = CoreConfiguration.class)
public class SessionsConfiguration<S extends Session> {

    @Autowired
    protected HttpSessionIdResolver sessionIdResolver;
    @Autowired
    protected ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    protected SessionRegistry sessionRegistry;

    private List<HttpSessionListener> httpSessionListeners = new ArrayList<>();

    public SessionRepositoryWrapper<S> sessionRepositoryWrapper(SessionRepository<S> sessionRepository) {
        SessionRepositoryWrapper<S> sessionRepositoryWrapper = new SessionRepositoryWrapper<>(
                sessionRegistry, applicationEventPublisher, sessionRepository);
        sessionRepositoryWrapper.addAttributePersistenceValidators(new VaadinSessionAttributesValidator());
        return sessionRepositoryWrapper;
    }

    @Bean
    @Primary
    public SessionRepositoryFilter<SessionRepositoryWrapper<S>.SessionWrapper> jmixSessionRepositoryFilter(
            @Autowired SessionRepository<S> sessionRepository) {
        SessionRepositoryFilter<SessionRepositoryWrapper<S>.SessionWrapper> sessionRepositoryFilter
                = new SessionRepositoryFilter<>(sessionRepositoryWrapper(sessionRepository));
        sessionRepositoryFilter.setHttpSessionIdResolver(sessionIdResolver);
        return sessionRepositoryFilter;
    }


    @Bean
    public SessionEventHttpSessionListenerAdapter sessionEventHttpSessionListenerAdapter() {
        return new SessionEventHttpSessionListenerAdapter(this.httpSessionListeners);
    }

    @Autowired(required = false)
    public void setHttpSessionListeners(List<HttpSessionListener> listeners) {
        this.httpSessionListeners = listeners;
    }
}

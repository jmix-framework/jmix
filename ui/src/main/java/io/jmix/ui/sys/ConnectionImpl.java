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

package io.jmix.ui.sys;

import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import com.vaadin.server.WebBrowser;
import com.vaadin.spring.annotation.VaadinSessionScope;
import io.jmix.core.ClientType;
import io.jmix.core.CoreProperties;
import io.jmix.core.Events;
import io.jmix.core.Messages;
import io.jmix.core.commons.events.EventHub;
import io.jmix.core.entity.User;
import io.jmix.core.security.*;
import io.jmix.ui.Connection;
import io.jmix.ui.events.*;
import io.jmix.ui.executors.BackgroundWorker;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 * Default {@link Connection} implementation for web-client.
 */
@Component(Connection.NAME)
@VaadinSessionScope
public class ConnectionImpl implements Connection {

    private static final Logger log = LoggerFactory.getLogger(ConnectionImpl.class);

    @Inject
    protected UserSessionManager userSessionManager;

    @Inject
    protected Events events;
    @Inject
    protected Messages messages;
    @Inject
    protected BackgroundWorker backgroundWorker;
    @Inject
    protected CoreProperties properties;

    // initial or used on login IP of the user
    protected String userRemoteAddress = null;

    protected EventHub eventHub = new EventHub();

    @Override
    public void login(Credentials credentials) throws LoginException {
        backgroundWorker.checkUIAccess();

        preprocessCredentials(credentials);

        UserSession userSession = loginInternal(credentials);

        if (credentials instanceof AnonymousUserCredentials) {
            userSession.setAuthenticated(false);
        } else {
            userSession.setAuthenticated(true);
        }

        UserSession previousSession = getSession();

        setSessionInternal(userSession);

        publishUserConnectedEvent(credentials);

        fireStateChangeListeners(previousSession, userSession);
    }

    protected UserSession createSession(UserSession userSession) {
        return new UserSession(userSession);
    }

    protected void preprocessCredentials(Credentials credentials) {
        if (credentials instanceof AbstractClientCredentials) {
            AbstractClientCredentials clientCredentials = (AbstractClientCredentials) credentials;
            clientCredentials.setClientType(new ClientType("WEB"));
            clientCredentials.setClientInfo(makeClientInfo());
            clientCredentials.setTimeZone(detectTimeZone());

            String currentUserRemoteAddress = getUserRemoteAddress();
            // update userRemoteAddress if current HTTP request is available
            if (currentUserRemoteAddress != null) {
                this.userRemoteAddress = currentUserRemoteAddress;
            }

            clientCredentials.setIpAddress(userRemoteAddress);
        }
    }

    @Nullable
    protected String getUserRemoteAddress() {
        VaadinRequest currentRequest = VaadinService.getCurrentRequest();
        return currentRequest != null ? currentRequest.getRemoteAddr() : null;
    }

    protected String makeClientInfo() {
        // timezone info is passed only on VaadinSession creation
        WebBrowser webBrowser = getWebBrowserDetails();

        //noinspection UnnecessaryLocalVariable
        String serverInfo = String.format("Web (%s:%s/%s) %s",
                properties.getWebHostName(),
                properties.getWebPort(),
                properties.getWebContextName(),
                webBrowser.getBrowserApplication());

        return serverInfo;
    }

    protected TimeZone detectTimeZone() {
        WebBrowser webBrowser = getWebBrowserDetails();

        int offset = webBrowser.getTimezoneOffset() / 1000 / 60;
        char sign = offset >= 0 ? '+' : '-';
        int absOffset = Math.abs(offset);

        String hours = StringUtils.leftPad(String.valueOf(absOffset / 60), 2, '0');
        String minutes = StringUtils.leftPad(String.valueOf(absOffset % 60), 2, '0');

        return TimeZone.getTimeZone("GMT" + sign + hours + minutes);
    }

    protected WebBrowser getWebBrowserDetails() {
        // timezone info is passed only on VaadinSession creation
        WebBrowser webBrowser = VaadinSession.getCurrent().getBrowser();
        VaadinRequest currentRequest = VaadinService.getCurrentRequest();
        // update web browser instance if current request is not null
        // it can be null in case of background/async processing of login request
        if (currentRequest != null) {
            webBrowser.updateRequestDetails(currentRequest);
        }
        return webBrowser;
    }

    protected UserSession loginInternal(Credentials credentials) throws LoginException {
        UserSession details = null;
        try {
            publishBeforeLoginEvent(credentials);

            // todo do we need this ?
            /*List<LoginProvider> providers = getProviders();

            for (LoginProvider provider : providers) {
                if (!provider.supports(credentialsClass)) {
                    continue;
                }

                log.trace("Login attempt using {}", provider.getClass().getName());

                try {
                    details = provider.login(credentials);

                    if (details != null) {
                        log.trace("Login successful for {}", credentials);

                        // publish login success
                        publishUserSessionStartedEvent(credentials, details);

                        return details;
                    }
                } catch (LoginException e) {
                    // publish auth fail
                    publishLoginFailed(credentials, provider, e);

                    throw e;
                } catch (RuntimeException re) {
                    InternalAuthenticationException ie =
                            new InternalAuthenticationException("Exception is thrown by login provider", re);

                    // publish auth fail
                    publishLoginFailed(credentials, provider, ie);

                    throw ie;
                }
            }*/

            details = userSessionManager.createSession(credentials);

            return details;
        } finally {
            publishAfterLoginEvent(credentials, details);
        }
    }

    protected void fireStateChangeListeners(UserSession previousSession, UserSession newSession) {
        StateChangeEvent event = new StateChangeEvent(this, previousSession, newSession);
        eventHub.publish(StateChangeEvent.class, event);
    }

    // todo substituteUser
    /*protected void fireSubstitutionListeners() {
        UserSubstitutedEvent event = new UserSubstitutedEvent(this);
        eventHub.publish(UserSubstitutedEvent.class, event);
    }*/

    protected void publishUserConnectedEvent(Credentials credentials) {
        events.publish(new UserConnectedEvent(this, credentials));
    }

    protected void publishBeforeLoginEvent(Credentials credentials) throws LoginException {
        events.publish(new BeforeLoginEvent(credentials));
    }

    protected void publishAfterLoginEvent(Credentials credentials, UserSession userSession) {
        events.publish(new AfterLoginEvent(credentials, userSession));
    }

    /*protected void publishLoginFailed(Credentials credentials, LoginProvider provider, LoginException e)
            throws LoginException {
        events.publish(new LoginFailureEvent(credentials, provider, e));
    }

    protected void publishUserSessionStartedEvent(Credentials credentials, AuthenticationDetails authenticationDetails) {
        events.publish(new UserSessionStartedEvent(this, credentials, authenticationDetails));
    }
*/
    protected UserSession getSessionInternal() {
        return VaadinSession.getCurrent().getAttribute(UserSession.class);
    }

    protected void setSessionInternal(UserSession userSession) {
        VaadinSession.getCurrent().setAttribute(UserSession.class, userSession);
        // todo set security context
        /*if (userSession != null) {
            AppContext.setSecurityContext(new SecurityContext(userSession));
        } else {
            AppContext.setSecurityContext(null);
        }*/
    }

    @Override
    public void logout() {
        backgroundWorker.checkUIAccess();

        UserSession session = getSessionInternal();

        if (session == null) {
            throw new IllegalStateException("There is no active session");
        }
        if (!session.isAuthenticated()) {
            throw new IllegalStateException("Active session is not authenticated");
        }

        try {
            userSessionManager.removeSession();
        } catch (NoUserSessionException e) {
            log.debug("An attempt to perform logout for expired session: {}", session, e);
        }

        publishUserSessionFinishedEvent(session);

        UserSession previousSession = getSession();

        setSessionInternal(null);

        eventHub.unsubscribe(UserSubstitutedEvent.class);

        publishDisconnectedEvent(previousSession);

        fireStateChangeListeners(previousSession, null);
    }

    protected void publishUserSessionFinishedEvent(UserSession session) {
        events.publish(new UserSessionFinishedEvent(this, session));
    }

    protected void publishUserSessionSubstitutedEvent(UserSession previousSession, UserSession session) {
        events.publish(new UserSessionSubstitutedEvent(this, previousSession, session));
    }

    protected void publishDisconnectedEvent(UserSession previousSession) {
        events.publish(new UserDisconnectedEvent(this, previousSession));
    }

    @Override
    @Nullable
    public UserSession getSession() {
        return getSessionInternal();
    }

    @Override
    public void substituteUser(User substitutedUser) {
        // todo substituteUser
        /*UserSession previousSession = getSession();

        UserSession session = authenticationService.substituteUser(substitutedUser);

        UserSession UserSession = createSession(session);
        UserSession.setAuthenticated(true);

        setSessionInternal(UserSession);

        publishUserSessionSubstitutedEvent(previousSession, UserSession);

        fireSubstitutionListeners();*/
    }

    @Override
    public boolean isConnected() {
        return getSessionInternal() != null;
    }

    @Override
    public boolean isAuthenticated() {
        UserSession session = getSessionInternal();
        return session != null && session.isAuthenticated();
    }

    @Override
    public boolean isAlive() {
        if (!isConnected()) {
            return false;
        }

        UserSession session = getSession();
        if (session == null) {
            return false;
        }

        // todo session ping
        /*try {
            userSessionService.getUserSession(session.getId());
        } catch (NoUserSessionException ignored) {
            return false;
        }*/

        return true;
    }

    @Override
    public void addStateChangeListener(Consumer<StateChangeEvent> listener) {
        eventHub.subscribe(StateChangeEvent.class, listener);
    }

    @Override
    public void removeStateChangeListener(Consumer<StateChangeEvent> listener) {
        eventHub.unsubscribe(StateChangeEvent.class, listener);
    }

    @Override
    public void addUserSubstitutionListener(Consumer<UserSubstitutedEvent> listener) {
        eventHub.subscribe(UserSubstitutedEvent.class, listener);
    }

    @Override
    public void removeUserSubstitutionListener(Consumer<UserSubstitutedEvent> listener) {
        eventHub.unsubscribe(UserSubstitutedEvent.class, listener);
    }

    @PostConstruct
    protected void init() {
        this.userRemoteAddress = getUserRemoteAddress();
    }
}
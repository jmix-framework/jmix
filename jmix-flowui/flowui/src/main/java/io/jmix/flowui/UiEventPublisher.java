/*
 * Copyright 2022 Haulmont.
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

package io.jmix.flowui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;
import io.jmix.core.cluster.ClusterApplicationEvent;
import io.jmix.core.cluster.ClusterApplicationEventPublisher;
import io.jmix.core.security.CurrentAuthentication;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.sys.SessionHolder;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Sends application events that should be handled in the components (e.g. views). To enable handling
 * application events in the {@link View}, annotate a method with {@link EventListener}. For instance:
 * <pre>
 *     &#064;EventListener
 *     public void customUiEventHandler(CustomUiEvent event) {
 *         // handle event
 *     }
 * </pre>
 * To correctly update the UI, class that implements {@link AppShellConfigurator} should contain the {@link Push}
 * annotation. It can be the main Spring Boot application class:
 * <pre>
 *     &#064;Push
 *     &#064;SpringBootApplication
 *     public class MyDemoProjectApplication implements AppShellConfigurator {
 *        // configuration
 *     }
 * </pre>
 */
@Component("flowui_UiEventPublisher")
public class UiEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(UiEventPublisher.class);

    protected CurrentAuthentication currentAuthentication;
    protected SystemAuthenticator systemAuthenticator;
    protected ClusterApplicationEventPublisher clusterApplicationEventPublisher;
    protected SessionHolder sessionHolder;

    public UiEventPublisher(SystemAuthenticator systemAuthenticator,
                            ClusterApplicationEventPublisher clusterApplicationEventPublisher,
                            SessionHolder sessionHolder, CurrentAuthentication currentAuthentication) {
        this.systemAuthenticator = systemAuthenticator;
        this.clusterApplicationEventPublisher = clusterApplicationEventPublisher;
        this.sessionHolder = sessionHolder;
        this.currentAuthentication = currentAuthentication;
    }

    @EventListener
    public void onUiUserEvent(UiUserEvent event) {
        publishEventForUsersInternal(event.getEvent(), event.getUsernames());
    }

    protected void publishEventForUsersInternal(ApplicationEvent event, @Nullable Collection<String> usernames) {
        Map<String, List<VaadinSession>> userSessions = sessionHolder.getActiveSessionsForUsernames(usernames);
        sendEventToUserSessions(event, userSessions);
    }

    protected void sendEventToUserSessions(ApplicationEvent event, Map<String, List<VaadinSession>> userSessions) {
        log.debug("Sending {} to {} Vaadin sessions", event, userSessions.values().stream().mapToLong(List::size).sum());

        for (Map.Entry<String, List<VaadinSession>> usernameSessionEntry : userSessions.entrySet()) {
            // Without 'VaadinAwareSecurityContextHolderStrategyConfiguration' configuration
            // when we get access to another user session, the security context is still the same as
            // in VaadinSession of sender user. I.e. if "admin" send notification to "user1", the
            // "CurrentAuthentication#getUser()" under VaadinSession of "user1" will return "admin".

            // To avoid the problem we should perform access to VaadinSession of recipient behalf of
            // recipient.
            String sessionUsername = usernameSessionEntry.getKey();
            List<VaadinSession> sessions = usernameSessionEntry.getValue();
            for (VaadinSession session : sessions) {
                systemAuthenticator.runWithUser(sessionUsername,
                        // obtain lock on session state
                        () -> session.access(() -> onSessionAccess(session, event)));
            }
        }
    }

    protected void onSessionAccess(VaadinSession session, ApplicationEvent event) {
        if (session.getState() != VaadinSessionState.OPEN) {
            return;
        }
        // notify all opened web browser tabs
        Collection<UI> uis = session.getUIs();
        for (UI ui : uis) {
            if (!ui.isClosing()) {
                // work in context of UI
                ui.accessSynchronously(() ->
                        session.getAttribute(UiEventsManager.class).publish(Collections.singletonList(ui), event));
            }
        }
    }

    /**
     * Publishes event only for the current UI.
     *
     * @param event application event
     */
    public void publishEventForCurrentUI(ApplicationEvent event) {
        publish(Collections.singletonList(UI.getCurrent()), event);
    }

    /**
     * Publishes event for all UIs (tabs and browsers) in the current user session.
     *
     * @param event application event
     */
    public void publishEvent(ApplicationEvent event) {
        publishEventForUsersInternal(event, List.of(currentAuthentication.getUser().getUsername()));
    }

    protected void publish(Collection<UI> uis, ApplicationEvent event) {
        VaadinSession vaadinSession = VaadinSession.getCurrent();
        if (vaadinSession != null) {
            vaadinSession.getAttribute(UiEventsManager.class).publish(uis, event);
        } else {
            throw new IllegalStateException("Event cannot be sent since there is no active Session instance");
        }
    }

    /**
     * Publishes the event for all UIs in all sessions of users specified in usernames collection.
     * If usernames collection is null the event will be published for all users.
     *
     * @param event     event to publish
     * @param usernames usernames of target users or null if broadcast to all users is needed
     */
    public void publishEventForUsers(ApplicationEvent event, @Nullable Collection<String> usernames) {
        UiUserEvent uiUserEvent = new UiUserEvent(this, event, usernames);
        clusterApplicationEventPublisher.publish(uiUserEvent);
    }

    /**
     * Event that should be processed on UI of specific users (or all users if usernames collection is null)
     */
    public static class UiUserEvent extends ClusterApplicationEvent {

        protected ApplicationEvent event;
        protected Collection<String> usernames;

        public UiUserEvent(Object source, ApplicationEvent event, @Nullable Collection<String> usernames) {
            super(source);
            this.event = event;
            this.usernames = usernames;
        }

        public ApplicationEvent getEvent() {
            return event;
        }

        @Nullable
        public Collection<String> getUsernames() {
            return usernames;
        }

        @Override
        public String toString() {
            return "UiUserEvent{" +
                    "event=" + event +
                    ", usernames=" + usernames +
                    '}';
        }
    }
}

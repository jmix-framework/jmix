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

import com.google.common.base.Strings;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.server.VaadinSessionState;
import com.vaadin.flow.server.WrappedSession;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.flowui.sys.event.UiEventsManager;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    protected ApplicationContext applicationContext;
    protected SystemAuthenticator systemAuthenticator;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final List<WeakReference<VaadinSession>> sessions = new ArrayList<>();

    public UiEventPublisher(ApplicationContext applicationContext, SystemAuthenticator systemAuthenticator) {
        this.applicationContext = applicationContext;
        this.systemAuthenticator = systemAuthenticator;
    }

    /**
     * Handles {@link VaadinServiceInitEventPublisher.VaadinSessionInitEvent} to collect new active
     * Vaadin sessions.
     *
     * @param event session initialized event
     */
    @EventListener
    public void onSessionInitialized(VaadinServiceInitEventPublisher.VaadinSessionInitEvent event) {
        lock.writeLock().lock();
        try {
            sessions.add(new WeakReference<>(event.getSession()));
            log.trace("Added session: " + event.getSession());
        } finally {
            lock.writeLock().unlock();
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
     * Publishes event for all UIs in the current session.
     *
     * @param event application event
     */
    public void publishEvent(ApplicationEvent event) {
        publish(Collections.emptyList(), event);
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
     * If usernames collection is null the event will be published for all users (broadcast).
     * @param event event to publish
     * @param usernames usernames of target users or null if broadcast to all users is needed
     */
    public void publishEventForUsers(ApplicationEvent event, @Nullable Collection<String> usernames) {
        Map<String, List<VaadinSession>> userActiveSessions = new HashMap<>();
        Set<String> usernamesSet = usernames != null ? new HashSet<>(usernames) : null;
        int removed = 0;

        lock.readLock().lock();
        try {
            for (Iterator<WeakReference<VaadinSession>> iterator = sessions.iterator(); iterator.hasNext(); ) {
                WeakReference<VaadinSession> reference = iterator.next();
                VaadinSession session = reference.get();
                if (session != null) {
                    String sessionUsername = getUsernameFromVaadinSession(session);
                    if (Strings.isNullOrEmpty(sessionUsername)) {
                        log.debug("Skip Vaadin session {} as it does not contain security context or" +
                                " authentication with username", session);
                    } else if (usernamesSet == null || usernamesSet.contains(sessionUsername)) {
                        List<VaadinSession> vaadinSessions =
                                userActiveSessions.computeIfAbsent(sessionUsername, k -> new ArrayList<>());
                        vaadinSessions.add(session);
                    }
                } else {
                    lock.readLock().unlock();
                    lock.writeLock().lock();
                    try {
                        iterator.remove();
                        lock.readLock().lock();
                    } finally {
                        lock.writeLock().unlock();
                    }
                    removed++;
                }
            }
        } finally {
            lock.readLock().unlock();
        }

        if (removed > 0) {
            log.debug("Removed {} Vaadin sessions", removed);
        }
        log.debug("Sending {} to {} Vaadin sessions", event,
                userActiveSessions.values().stream().mapToLong(List::size).sum());

        for (Map.Entry<String, List<VaadinSession>> usernameSessionEntry : userActiveSessions.entrySet()) {
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

    @Nullable
    protected String getUsernameFromVaadinSession(VaadinSession session) {
        WrappedSession wrappedSession = session.getSession();
        if (wrappedSession == null) {
            return null;
        }
        SecurityContext securityContext = (SecurityContext) wrappedSession.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext == null || securityContext.getAuthentication() == null) {
            return null;
        }
        Authentication authentication = securityContext.getAuthentication();
        if (!(authentication.getPrincipal() instanceof UserDetails)) {
            return null;
        }
        return ((UserDetails) authentication.getPrincipal()).getUsername();
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
}

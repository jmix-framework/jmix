/*
 * Copyright 2023 Haulmont.
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

package io.jmix.flowui.sys;

import com.google.common.base.Strings;
import com.vaadin.flow.server.*;
import io.jmix.core.annotation.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Holds vaadin sessions for all users
 */
@Internal
@Component("flowui_SessionHolder")
public class SessionHolder implements VaadinServiceInitListener {

    private static final Logger log = LoggerFactory.getLogger(SessionHolder.class);

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();
    protected final List<WeakReference<VaadinSession>> sessions = new ArrayList<>();

    /**
     * Provides active (not collected by gc) vaadin sessions mapped on specified usernames
     *
     * @param usernames usernames of users which sessions should be provided.
     *                  Null means that sessions for all users will be returned.
     * @return active user vaadin sessions
     */
    public Map<String, List<VaadinSession>> getActiveSessionsForUsernames(@Nullable Collection<String> usernames) {
        Map<String, List<VaadinSession>> userActiveSessions = new HashMap<>();
        Set<String> usernamesSet = usernames != null ? new HashSet<>(usernames) : null;
        int removed = 0;
        lock.readLock().lock();
        try {
            for (Iterator<WeakReference<VaadinSession>> iterator = sessions.iterator(); iterator.hasNext(); ) {
                WeakReference<VaadinSession> reference = iterator.next();
                VaadinSession session = reference.get();
                if (session != null
                        && !getVaadinSessionState(session).equals(VaadinSessionState.CLOSED)) {
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
        return userActiveSessions;
    }

    @Nullable
    protected String getUsernameFromVaadinSession(VaadinSession session) {
        WrappedSession wrappedSession = session.getSession();
        if (wrappedSession == null
                || !getVaadinSessionState(session).equals(VaadinSessionState.OPEN)) {
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

    protected VaadinSessionState getVaadinSessionState(VaadinSession session) {
        VaadinSessionState state;
        if (session.hasLock()) {
            state = session.getState();
        } else {
            // When session is 'CLOSED' it may not have lock,
            // but we still need to get its state
            try {
                session.lock();
                state = session.getState();
            } finally {
                session.unlock();
            }
        }

        return state;
    }

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addSessionInitListener(this::onSessionInit);
        event.getSource().addSessionDestroyListener(this::onSessionDestroy);
    }

    protected void onSessionInit(SessionInitEvent event) {
        lock.writeLock().lock();
        try {
            sessions.add(new WeakReference<>(event.getSession()));
            log.trace("Added session: {}", event.getSession());
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected void onSessionDestroy(SessionDestroyEvent event) {
        lock.writeLock().lock();
        try {
            boolean removed = sessions.removeIf(ref ->
                    ref.refersTo(event.getSession()) || ref.refersTo(null)
            );

            if (removed) {
                log.trace("Removed session: {}", event.getSession());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
}

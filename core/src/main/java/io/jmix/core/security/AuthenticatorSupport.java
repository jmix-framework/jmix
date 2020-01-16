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

package io.jmix.core.security;

import io.jmix.core.security.impl.SystemSessions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.UUID;
import java.util.function.Supplier;

public abstract class AuthenticatorSupport {

    private static final Logger log = LoggerFactory.getLogger(AuthenticatorSupport.class);

    protected static final UserSession NULL_SESSION = new NullSession();

    protected ThreadLocal<Deque<Authentication>> threadLocalStack = new ThreadLocal<>();

    protected SystemSessions sessions;

    public AuthenticatorSupport(SystemSessions sessions) {
        this.sessions = sessions;
    }

    protected UserSession getFromCacheOrCreate(String login, Supplier<UserSession> supplier) {
        UserSession session = sessions.get(login);
        if (session == null) {
            // saved session doesn't exist
            synchronized (this) {
                // double check to prevent the same log in by subsequent threads
                session = sessions.get(login);
                if (session == null) {
                    try {
                        session = supplier.get();
                    } catch (LoginException e) {
                        throw new RuntimeException("Unable to perform system login", e);
                    }
                    sessions.put(login, session);
                }
            }
        }
        return session;
    }

    protected void pushAuthentication(@Nullable Authentication authentication) {
        Deque<Authentication> stack = threadLocalStack.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            threadLocalStack.set(stack);
        } else {
            if (stack.size() > 10) {
                log.warn("Stack is too big: {}. Check correctness of begin/end invocations.", stack.size());
            }
        }
        if (authentication == null) {
            stack.push(NULL_SESSION);
        } else {
            stack.push(authentication);
        }
    }

    @Nullable
    protected Authentication popAuthentication() {
        Deque<Authentication> stack = threadLocalStack.get();
        if (stack != null) {
            Authentication authentication = stack.poll();
            if (authentication != null) {
                if (authentication == NULL_SESSION) {
                    return null;
                } else {
                    return authentication;
                }
            } else {
                log.warn("Stack is empty. Check correctness of begin/end invocations.");
            }
        } else {
            log.warn("Stack does not exist. Check correctness of begin/end invocations.");
        }
        return null;
    }

    protected static class NullSession extends UserSession {

        private static final long serialVersionUID = 5437664860036209641L;

        public NullSession() {
            id = new UUID(0L, 0L);
        }
    }

}

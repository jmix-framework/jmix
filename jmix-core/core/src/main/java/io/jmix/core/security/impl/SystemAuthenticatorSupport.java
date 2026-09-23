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

package io.jmix.core.security.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Keeps a per-thread stack of {@link SecurityContext} instances that were current before each
 * {@code begin()} call, so that {@code end()} can reinstall the exact previous instance.
 * <p>
 * The stored instances are never modified.
 */
public abstract class SystemAuthenticatorSupport {

    private static final Logger log = LoggerFactory.getLogger(SystemAuthenticatorSupport.class);

    protected ThreadLocal<Deque<SecurityContext>> threadLocalStack = new ThreadLocal<>();

    public SystemAuthenticatorSupport() {
    }

    protected void pushSecurityContext(SecurityContext securityContext) {
        Deque<SecurityContext> stack = threadLocalStack.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            threadLocalStack.set(stack);
        } else {
            if (stack.size() > 10) {
                log.warn("Stack is too big: {}. Check correctness of begin/end invocations.", stack.size());
            }
        }
        stack.push(securityContext);
    }

    @Nullable
    protected SecurityContext pollSecurityContext() {
        Deque<SecurityContext> stack = threadLocalStack.get();
        if (stack != null) {
            SecurityContext securityContext = stack.poll();
            if (securityContext != null) {
                return securityContext;
            } else {
                log.warn("Stack is empty. Check correctness of begin/end invocations.");
            }
        } else {
            log.warn("Stack does not exist. Check correctness of begin/end invocations.");
        }
        return null;
    }
}

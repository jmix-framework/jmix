/*
 * Copyright 2026 Haulmont.
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

import com.vaadin.flow.spring.security.VaadinAwareSecurityContextHolderStrategy;
import io.jmix.core.security.SystemAuthenticator;
import io.jmix.core.security.ThreadSecurityContextOverride;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * {@link SecurityContextHolderStrategy} for FlowUI applications.
 * <p>
 * Delegates to Vaadin's {@link VaadinAwareSecurityContextHolderStrategy}, which prefers the security context stored
 * in the current Vaadin session over the thread-local one. On top of that, it lets {@link SystemAuthenticator}
 * install a context that takes precedence for the current thread only, so that {@code begin()}/{@code end()} take
 * effect on UI threads and never modify the context shared by all threads of the HTTP session.
 * <p>
 * {@link #setContext(SecurityContext)} and {@link #clearContext()} drop any pending override, so that an unbalanced
 * {@code begin()} cannot leak to the next request or task executed on a pooled thread.
 */
public class JmixSecurityContextHolderStrategy implements SecurityContextHolderStrategy, ThreadSecurityContextOverride {

    private final SecurityContextHolderStrategy delegate;

    private final ThreadLocal<Deque<SecurityContext>> overrides = new ThreadLocal<>();

    public JmixSecurityContextHolderStrategy() {
        this(new VaadinAwareSecurityContextHolderStrategy());
    }

    public JmixSecurityContextHolderStrategy(SecurityContextHolderStrategy delegate) {
        this.delegate = delegate;
    }

    @Override
    public SecurityContext getContext() {
        Deque<SecurityContext> stack = overrides.get();
        if (stack != null && !stack.isEmpty()) {
            return stack.peek();
        }
        return delegate.getContext();
    }

    @Override
    public Supplier<SecurityContext> getDeferredContext() {
        Deque<SecurityContext> stack = overrides.get();
        if (stack != null && !stack.isEmpty()) {
            SecurityContext context = stack.peek();
            return () -> context;
        }
        return delegate.getDeferredContext();
    }

    @Override
    public void setContext(SecurityContext context) {
        overrides.remove();
        delegate.setContext(context);
    }

    @Override
    public void setDeferredContext(Supplier<SecurityContext> deferredContext) {
        overrides.remove();
        delegate.setDeferredContext(deferredContext);
    }

    @Override
    public void clearContext() {
        overrides.remove();
        delegate.clearContext();
    }

    @Override
    public SecurityContext createEmptyContext() {
        return delegate.createEmptyContext();
    }

    @Override
    public void pushContext(SecurityContext context) {
        Deque<SecurityContext> stack = overrides.get();
        if (stack == null) {
            stack = new ArrayDeque<>();
            overrides.set(stack);
        }
        stack.push(context);
    }

    @Override
    public void popContext() {
        Deque<SecurityContext> stack = overrides.get();
        if (stack != null) {
            stack.poll();
            if (stack.isEmpty()) {
                overrides.remove();
            }
        }
    }
}

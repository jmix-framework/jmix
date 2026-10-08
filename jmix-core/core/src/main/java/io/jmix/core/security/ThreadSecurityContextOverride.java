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

package io.jmix.core.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;

/**
 * Optional capability of a {@link SecurityContextHolderStrategy}: lets the current thread temporarily override the
 * {@link SecurityContext} returned by {@link SecurityContextHolder#getContext()}, regardless of where the strategy
 * would otherwise take the context from (for example, from the current Vaadin session).
 * <p>
 * Used by {@link SystemAuthenticator} to make {@code begin()}/{@code end()} affect the current thread only.
 * If the installed strategy does not implement this interface, {@link SystemAuthenticator} falls back to
 * {@link SecurityContextHolder#setContext(SecurityContext)}, which is sufficient for a plain thread-local strategy.
 * <p>
 * Implementations must keep a per-thread stack so that nested overrides work.
 * {@link SecurityContextHolderStrategy#setContext(SecurityContext)} must replace the current override without
 * discarding the enclosing scopes. {@link SecurityContextHolderStrategy#clearContext()} must clear the stack
 * when a request or task releases its thread.
 */
@NullMarked
public interface ThreadSecurityContextOverride {

    /**
     * Makes the given context the one returned by {@code getContext()} on the current thread until a matching
     * {@link #popContext()} call.
     */
    void pushContext(SecurityContext context);

    /**
     * Removes the most recently pushed override from the current thread. Does nothing if there is no override.
     */
    void popContext();
}

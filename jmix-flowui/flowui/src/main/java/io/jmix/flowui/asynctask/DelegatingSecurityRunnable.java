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

package io.jmix.flowui.asynctask;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Wraps a delegate {@link Runnable} with logic for setting up an {@link SecurityContext} before invoking the delegate
 * {@link Runnable} and then removing the {@link SecurityContext} after the delegate has completed.
 * <p>
 * If there is a {@link SecurityContext} that already exists, it will be restored after the {@link #run()} method is
 * invoked.
 */
public class DelegatingSecurityRunnable implements Runnable {

    private final Runnable delegate;
    private final SecurityContext securityContext;

    public DelegatingSecurityRunnable(Runnable delegate) {
        this(delegate, SecurityContextHolder.getContext());
    }

    public DelegatingSecurityRunnable(Runnable delegate, SecurityContext securityContext) {
        this.delegate = delegate;
        this.securityContext = securityContext;
    }

    @Override
    public void run() {
        SecurityContext originalSecurityContext = SecurityContextHolder.getContext();
        try {
            SecurityContextHolder.setContext(securityContext);
            delegate.run();
        } finally {
            SecurityContextHolder.setContext(originalSecurityContext);
        }
    }
}

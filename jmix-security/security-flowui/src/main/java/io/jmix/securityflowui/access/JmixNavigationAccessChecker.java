/*
 * Copyright 2024 Haulmont.
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

package io.jmix.securityflowui.access;

import com.vaadin.flow.server.auth.AccessCheckResult;
import com.vaadin.flow.server.auth.NavigationAccessChecker;
import com.vaadin.flow.server.auth.NavigationContext;
import io.jmix.flowui.sys.UiAccessChecker;

/**
 * A {@link NavigationAccessChecker} that checks access to view by the presence of the  Vaadin
 * <code>@AnonymousAllowed</code> annotation on the view controller and by analyzing Jmix resource roles assigned for
 * the current user.
 */
public class JmixNavigationAccessChecker implements NavigationAccessChecker {

    protected final UiAccessChecker uiAccessChecker;

    public JmixNavigationAccessChecker(UiAccessChecker uiAccessChecker) {
        this.uiAccessChecker = uiAccessChecker;
    }

    @Override
    public AccessCheckResult check(NavigationContext context) {
        Class<?> targetView = context.getNavigationTarget();
        boolean viewPermitted = uiAccessChecker.isViewPermitted(targetView, context.getPrincipal(), context::hasRole);
        if (viewPermitted) {
            return context.allow();
        } else {
            return context.deny("Access is denied by Jmix security subsystem");
        }
    }
}
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

package io.jmix.flowui.testassist.vaadin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.internal.ExecutionContext;
import com.vaadin.flow.internal.StateTree.ExecutionRegistration;
import com.vaadin.flow.server.VaadinRequest;

/**
 * {@link UI} for integration tests that emulates the client response cycle.
 * <p>
 * In a running application, tasks registered via {@link UI#beforeClientResponse} are collected by
 * the {@code StateTree} during request handling and executed as a single batch before the response
 * is sent to the client. There is no client-server communication in tests, so these tasks would
 * never run, leaving components that defer initialization partially initialized — e.g.
 * {@code JmixTabSheet} attaches the selected tab content to the component tree in such a callback.
 * <p>
 * This UI emulates the cycle in two phases:
 * <ul>
 *     <li>while a navigation is being handled, callbacks are only collected, then executed as one
 *     batch once the navigation finishes — exactly as a real request behaves;</li>
 *     <li>at any other time there is no response to wait for, so each callback registered via
 *     {@link #beforeClientResponse(Component, SerializableConsumer)} runs immediately, and
 *     programmatic changes or dialogs opened without navigation are handled without any explicit
 *     flush.</li>
 * </ul>
 * Known deviations from a running application:
 * <ul>
 *     <li>only registrations made through {@link UI#beforeClientResponse} trigger the immediate
 *     run; code that registers directly on the {@code StateTree} (e.g. {@code Element#executeJs}
 *     or data communicator flushes) is collected and executed at the next flush — the next
 *     navigation or {@code beforeClientResponse} call — or explicitly via
 *     {@link #runExecutionsBeforeClientResponse()};</li>
 *     <li>the immediate run happens synchronously inside the registering call, whereas in a
 *     running application the callback runs at the end of the request — caller code following the
 *     registration that the callback depends on (e.g.
 *     {@code field = ui.beforeClientResponse(owner, ctx -> field = null)}) observes the inverted
 *     order.</li>
 * </ul>
 */
public class TestUI extends UI {

    protected boolean runningExecutions;

    @Override
    public void doInit(VaadinRequest request, int uiId, String appId) {
        super.doInit(request, uiId, appId);

        // The listener is registered here, not in the constructor, because it requires the UI
        // session, which is set after instantiation.
        addAfterNavigationListener(event -> runExecutionsBeforeClientResponse());
    }

    @Override
    public ExecutionRegistration beforeClientResponse(Component component,
                                                      SerializableConsumer<ExecutionContext> execution) {
        ExecutionRegistration registration = super.beforeClientResponse(component, execution);
        // 'lastHandledNavigation' is non-null exactly while the Router is handling a navigation
        // ('Router#navigate' clears it in 'finally' even if the navigation fails), so callbacks
        // registered during navigation are left to the AfterNavigationListener batch, and at any
        // other time they run at once. Unlike a mutable phase flag, this cannot get stuck after a
        // failed navigation and requires no initial navigation to activate.
        if (!getInternals().hasLastHandledLocation()) {
            runExecutionsBeforeClientResponse();
        }
        return registration;
    }

    /**
     * Runs the tasks collected via {@link UI#beforeClientResponse}, emulating the batch the
     * framework executes before a client response.
     */
    public void runExecutionsBeforeClientResponse() {
        // Guard against reentrancy: an execution may register new tasks, but those are picked up by
        // the loop inside StateTree#runExecutionsBeforeClientResponse, so no nested call is needed.
        if (runningExecutions) {
            return;
        }
        runningExecutions = true;
        try {
            getInternals().getStateTree().runExecutionsBeforeClientResponse();
        } finally {
            runningExecutions = false;
        }
    }
}

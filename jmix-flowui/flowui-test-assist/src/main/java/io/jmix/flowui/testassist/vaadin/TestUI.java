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
 *     <li>while a navigation is in progress, callbacks are only collected, then executed as one
 *     batch once the navigation finishes — exactly as a real request behaves;</li>
 *     <li>afterwards the UI acts as if the client has responded: each subsequently registered
 *     callback runs immediately, so programmatic changes or dialogs opened after navigation (with
 *     no further navigation) are handled without any explicit flush.</li>
 * </ul>
 */
public class TestUI extends UI {

    // True once a navigation has finished: the UI then behaves as if the client responded, running
    // each newly registered callback immediately. Reset when a new navigation starts, so every
    // navigation is handled as its own request (collect during, flush at the end).
    protected boolean clientResponded;

    protected boolean runningExecutions;

    @Override
    public void doInit(VaadinRequest request, int uiId, String appId) {
        super.doInit(request, uiId, appId);

        // Listeners are registered here, not in the constructor, because they require the UI
        // session, which is set after instantiation.
        addBeforeEnterListener(event -> clientResponded = false);
        addAfterNavigationListener(event -> {
            runExecutionsBeforeClientResponse();
            clientResponded = true;
        });
    }

    @Override
    public ExecutionRegistration beforeClientResponse(Component component,
                                                      SerializableConsumer<ExecutionContext> execution) {
        ExecutionRegistration registration = super.beforeClientResponse(component, execution);
        if (clientResponded) {
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

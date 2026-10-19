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

package io.jmix.aitoolsflowui.service;

import io.jmix.aitoolsflowui.model.AiChatMessage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * What the assistant should respond to and the options of the call, passed to
 * {@link AiChatService#generateResponse(AiChatResponseSpec)}.
 */
@NullMarked
public class AiChatResponseSpec {

    protected AiChatMessage userMessage;

    @Nullable
    protected String correlationId;

    /**
     * Creates a spec for responding to the given message.
     *
     * @param userMessage the persisted user message to respond to
     */
    public AiChatResponseSpec(AiChatMessage userMessage) {
        checkNotNullArgument(userMessage, "userMessage is null");

        this.userMessage = userMessage;
    }

    /**
     * Returns the user message to respond to.
     *
     * @return the persisted user message
     */
    public AiChatMessage getUserMessage() {
        return userMessage;
    }

    /**
     * Returns the id that attributes the tool status events of the call to its caller.
     *
     * @return the correlation id, or {@code null} if none was set
     */
    @Nullable
    public String getCorrelationId() {
        return correlationId;
    }

    /**
     * Sets the id that attributes the tool status events of this call to its caller. When set, the tools' status
     * updates are published as {@link io.jmix.aitools.tool.AiToolStatusUpdateEvent}s carrying it; when absent, tools
     * report nothing.
     *
     * @param correlationId the correlation id, or {@code null} for none
     * @return this spec
     */
    public AiChatResponseSpec setCorrelationId(@Nullable String correlationId) {
        this.correlationId = correlationId;
        return this;
    }
}

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

package io.jmix.aitools.tool;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Lets AI tools surface ephemeral status updates while a model call is running.
 * <p>
 * A caller that wants the updates puts a correlation id into the {@link ToolContext} under
 * {@link #CORRELATION_ID}; each update is then published as an {@link AiToolStatusUpdateEvent} carrying that id.
 * Without the key (typical for non-UI callers) all methods are silent no-ops.
 * <p>
 * <b>Typical tool usage</b> — two-phase publish so a listener can show an in-flight step and then its result:
 * <pre>
 * String msg = "Searching customers by name";
 * statusPublisher.update(msg, toolContext);
 * List&lt;Customer&gt; result = customerRepo.searchByName(...);   // long-running work
 * statusPublisher.complete(msg, "found " + result.size(), toolContext);
 * </pre>
 */
@Component("aitls_AiToolStatusPublisher")
public class AiToolStatusPublisher {

    private static final Logger log = LoggerFactory.getLogger(AiToolStatusPublisher.class);

    /**
     * Key of the tool-context entry holding the correlation id of the model call. When present, every status
     * update is published as an {@link AiToolStatusUpdateEvent} carrying it.
     */
    public static final String CORRELATION_ID = "aitls_correlationId";

    /**
     * Key of the tool-context entry holding a {@code Consumer<AiToolStatusUpdate>} that receives status updates.
     *
     * @deprecated put a correlation id under {@link #CORRELATION_ID} and listen to {@link AiToolStatusUpdateEvent}
     */
    @Deprecated(since = "3.2", forRemoval = true)
    public static final String STATUS_UPDATE_CALLBACK = "aitls_statusUpdateCallback";

    /**
     * Key of the marker entry that the add-on places into the default {@link ToolContext} of its chat
     * clients, so it is visible to every tool invocation, including application-defined ones.
     * <p>
     * Only the presence of the key is part of the contract — its value is unspecified and may change.
     * An application whose tool iterates, logs or forwards the tool context should reference this
     * constant to recognise and skip the entry rather than hard-coding the string.
     */
    public static final String DEFAULT_TOOL_CONTEXT_MARKER = "aitls_defaultToolContext";

    @Autowired
    protected ApplicationEventPublisher eventPublisher;

    /**
     * Publishes an in-flight status update — "this step has started, no result yet".
     * Sends an {@link AiToolStatusUpdate} with a blank {@code resultSnippet}.
     *
     * @param message     status text describing the step that has started
     * @param toolContext current tool context; {@code null} or one without a correlation id makes this a no-op
     */
    public void update(String message, @Nullable ToolContext toolContext) {
        publish(new AiToolStatusUpdate(message), toolContext);
    }

    /**
     * Publishes the completion of a previously-started step.
     * <p>
     * The {@code message} must match the one passed to {@link #update(String, ToolContext)} so a listener
     * can fold the two into a single completed entry. A blank {@code snippet} is treated as
     * "nothing to report" and the call is a silent no-op.
     *
     * @param message     same status text that was passed to {@link #update(String, ToolContext)}
     * @param snippet     short result of the finished step; blank or {@code null} makes this a no-op
     * @param toolContext current tool context; {@code null} or one without a correlation id makes this a no-op
     */
    public void complete(String message, @Nullable String snippet, @Nullable ToolContext toolContext) {
        if (snippet == null || snippet.isBlank()) {
            return;
        }
        publish(new AiToolStatusUpdate(message, snippet), toolContext);
    }

    /**
     * Publishes an arbitrary {@link AiToolStatusUpdate} as an {@link AiToolStatusUpdateEvent}.
     * <p>
     * <b>Note, prefer:</b>
     * <ul>
     *     <li>
     *         {@link #update(String, ToolContext)}
     *     </li>
     *     <li>
     *         {@link #complete(String, String, ToolContext)}
     *     </li>
     * </ul>
     * which encode the two-phase contract correctly.
     *
     * <p>
     * A failing listener or callback is logged and never propagates: reporting a status must not break the tool.
     *
     * @param update      status update to deliver; ignored if its message is blank
     * @param toolContext current tool context; {@code null} or one without a correlation id (and without the
     *                    deprecated callback) makes this a no-op
     */
    @SuppressWarnings("unchecked")
    public void publish(AiToolStatusUpdate update, @Nullable ToolContext toolContext) {
        if (toolContext == null || update.getMessage().isBlank()) {
            return;
        }
        Map<String, Object> context = toolContext.getContext();
        if (context.get(CORRELATION_ID) instanceof String correlationId) {
            try {
                eventPublisher.publishEvent(new AiToolStatusUpdateEvent(this, correlationId, update));
            } catch (RuntimeException e) {
                log.warn("Failed to publish a tool status for correlation id {}", correlationId, e);
            }
        }
        if (context.get(STATUS_UPDATE_CALLBACK) instanceof Consumer<?> callback) {
            try {
                ((Consumer<AiToolStatusUpdate>) callback).accept(update);
            } catch (RuntimeException e) {
                log.warn("Tool status callback failed", e);
            }
        }
    }
}

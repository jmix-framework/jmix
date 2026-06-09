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

import io.jmix.aitools.service.AiConversationChatService;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Helper for AI tools that want to surface ephemeral status updates to the UI.
 * <p>
 * The callback is placed into the {@link ToolContext} by {@link AiConversationChatService}
 * under {@link #STATUS_UPDATE_CALLBACK}. If no callback was provided by the caller (typical for non-UI scenarios),
 * all methods on this publisher are silent no-ops.
 * <p>
 * <b>Typical tool usage</b> — two-phase publish so the UI can render an in-flight indicator and a result snippet:
 * <pre>
 * String msg = "Searching customers by name";
 * statusPublisher.update(toolContext, msg);
 * String result = customerRepo.searchByName(...);   // long-running work
 * statusPublisher.complete(toolContext, msg, "found " + result.size());
 * </pre>
 */
@Component("aitols_AiToolStatusPublisher")
public class AiToolStatusPublisher {

    public static final String STATUS_UPDATE_CALLBACK = "aitols_statusUpdateCallback";

    /**
     * Publishes an in-flight status update — "this step has started, no result yet".
     * Sends an {@link AiUiStatusUpdate} with a blank {@code resultSnippet}.
     *
     * @param message     status text describing the step that has started
     * @param toolContext current tool context carrying the UI callback; {@code null} (no callback) makes this a no-op
     */
    public void update(String message, @Nullable ToolContext toolContext) {
        publish(new AiUiStatusUpdate(message), toolContext);
    }

    /**
     * Publishes the completion of a previously-started step.
     * <p>
     * The {@code baseMessage} must match the one passed to {@link #update(String, ToolContext)} so the UI
     * can fold the two into a single completed entry. A blank {@code snippet} is treated as
     * "nothing to report" and the call is a silent no-op.
     *
     * @param baseMessage same status text that was passed to {@link #update(String, ToolContext)}
     * @param snippet     short result of the finished step; blank or {@code null} makes this a no-op
     * @param toolContext current tool context carrying the UI callback; {@code null} (no callback) makes this a no-op
     */
    public void complete(String baseMessage, @Nullable String snippet, @Nullable ToolContext toolContext) {
        if (snippet == null || snippet.isBlank()) {
            return;
        }
        publish(new AiUiStatusUpdate(baseMessage, snippet), toolContext);
    }

    /**
     * Forwards an arbitrary {@link AiUiStatusUpdate} through the UI callback.
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
     * @param update      status update to deliver; ignored if its message is blank
     * @param toolContext current tool context carrying the UI callback; {@code null} (no callback) makes this a no-op
     */
    @SuppressWarnings("unchecked")
    public void publish(AiUiStatusUpdate update, @Nullable ToolContext toolContext) {
        if (toolContext == null || update.message().isBlank()) {
            return;
        }
        Object raw = toolContext.getContext().get(STATUS_UPDATE_CALLBACK);
        if (!(raw instanceof Consumer<?>)) {
            return;
        }
        Consumer<AiUiStatusUpdate> callback = (Consumer<AiUiStatusUpdate>) raw;
        callback.accept(update);
    }
}

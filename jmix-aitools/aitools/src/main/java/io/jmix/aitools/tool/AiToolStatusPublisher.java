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
 * The callback is placed into the {@link ToolContext} by
 * {@link AiConversationChatService} under {@link #STATUS_UPDATE_CALLBACK}. If
 * no callback was provided by the caller (typical for non-UI scenarios), all
 * methods on this publisher are silent no-ops.
 * <p>
 * <b>Typical tool usage</b> — two-phase publish so the UI can render an
 * in-flight indicator and a result snippet:
 * <pre>{@code
 * String msg = "Searching customers by name";
 * statusPublisher.update(toolContext, msg);
 * String result = customerRepo.searchByName(...);   // long-running work
 * statusPublisher.complete(toolContext, msg, "found " + result.size());
 * }</pre>
 */
@Component("aitols_AiToolStatusPublisher")
public class AiToolStatusPublisher {

    public static final String STATUS_UPDATE_CALLBACK = "aitols_statusUpdateCallback";

    /**
     * Publishes an in-flight status update — "this step has started, no
     * result yet". Sends an {@link AiUiStatusUpdate} with a blank
     * {@code resultSnippet}.
     */
    public void update(@Nullable ToolContext toolContext, String message) {
        publish(toolContext, new AiUiStatusUpdate(message));
    }

    /**
     * Publishes the completion of a previously-started step. The
     * {@code baseMessage} must match the one passed to
     * {@link #update(ToolContext, String)} so the UI can fold the two into
     * a single completed entry. A blank {@code snippet} is treated as
     * "nothing to report" and the call is a silent no-op.
     */
    public void complete(@Nullable ToolContext toolContext,
                         String baseMessage,
                         @Nullable String snippet) {
        if (snippet == null || snippet.isBlank()) {
            return;
        }
        publish(toolContext, new AiUiStatusUpdate(baseMessage, snippet));
    }

    /**
     * Low-level publish: forwards an arbitrary {@link AiUiStatusUpdate}
     * through the UI callback. Prefer {@link #update(ToolContext, String)} /
     * {@link #complete(ToolContext, String, String)} which encode the
     * two-phase contract correctly.
     */
    @SuppressWarnings("unchecked")
    public void publish(@Nullable ToolContext toolContext, AiUiStatusUpdate update) {
        if (toolContext == null
                || update == null
                || update.message() == null
                || update.message().isBlank()) {
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

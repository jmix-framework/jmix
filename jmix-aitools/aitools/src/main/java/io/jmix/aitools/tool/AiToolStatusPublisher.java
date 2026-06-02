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
import io.jmix.aitools.service.AiUiStatusUpdate;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

/**
 * Helper for AI tools that want to surface ephemeral status updates to the UI.
 * <p>
 * The callback is placed into the {@link ToolContext} by {@link AiConversationChatService} under
 * {@link #STATUS_UPDATE_CALLBACK}. Tools call {@link #publish(ToolContext, AiUiStatusUpdate)};
 * if no callback was provided by the caller (typical for non-UI scenarios), the call is a silent no-op.
 */
@Component("aitols_AiToolStatusPublisher")
public class AiToolStatusPublisher {

    public static final String STATUS_UPDATE_CALLBACK = "aitols_statusUpdateCallback";

    @SuppressWarnings("unchecked")
    public void publish(@Nullable ToolContext toolContext, AiUiStatusUpdate update) {
        if (toolContext == null || update == null) {
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

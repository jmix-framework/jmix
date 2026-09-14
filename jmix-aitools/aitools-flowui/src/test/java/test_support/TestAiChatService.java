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

package test_support;

import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.service.AiChatService;

import java.util.function.Consumer;

/**
 * {@link AiChatService} for UI tests: reports the chat as available, so the views render as they do in a
 * configured application, and echoes any message instead of calling a model.
 */
public class TestAiChatService implements AiChatService {

    @Override
    public String processMessage(AiChatMessage message) {
        return message.getContent();
    }

    @Override
    public String processMessage(AiChatMessage message, Consumer<AiToolStatusUpdate> statusCallback) {
        return message.getContent();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }
}

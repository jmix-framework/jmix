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

package io.jmix.aitoolsflowui.service.impl;

import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.service.AiChatService;
import io.jmix.aitoolsflowui.service.AiConversationService;
import io.jmix.aitoolsflowui.service.AiChatMessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * No-op implementation.
 */
public class AiChatEmptyService implements AiChatService {

    private static final Logger log = LoggerFactory.getLogger(AiChatEmptyService.class);

    public AiChatEmptyService() {
        // Active only as the no-persistence fallback; warn once so a missing data module is noticed.
        log.warn("AI chat is running in no-op mode: no persistence implementation found. " +
                        "Add the 'jmix-aitools-flowui-data-starter', or provide your own {}, " +
                        "{} and {} beans, to enable AI chat.",
                AiChatService.class.getSimpleName(),
                AiConversationService.class.getSimpleName(),
                AiChatMessageService.class.getSimpleName());
    }

    @Override
    public String processMessage(AiChatMessage message) {
        return "";
    }

    @Override
    public String processMessage(AiChatMessage message, Consumer<AiToolStatusUpdate> statusCallback) {
        return "";
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}

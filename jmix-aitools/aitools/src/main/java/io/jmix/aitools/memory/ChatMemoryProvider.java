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

package io.jmix.aitools.memory;

import io.jmix.aitools.AiToolsProperties;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("aitols_ChatMemoryProvider")
public class ChatMemoryProvider {

    @Autowired
    protected JmixChatMemoryRepository chatMemoryRepository;

    @Autowired
    protected AiToolsProperties toolsProperties;

    private ChatMemory chatMemory;

    public ChatMemory getChatMemory() {
        if (chatMemory == null) {
            chatMemory = build();
        }
        return chatMemory;
    }

    public ChatMemory build() {
        return MessageWindowChatMemory.builder()
                .maxMessages(toolsProperties.getChatMemoryMaxMessages())
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }
}

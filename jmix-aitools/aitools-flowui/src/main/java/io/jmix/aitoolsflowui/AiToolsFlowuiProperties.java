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

package io.jmix.aitoolsflowui;

import io.jmix.aitoolsflowui.view.chat.AiConversationTitleMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

@ConfigurationProperties(prefix = "jmix.aitools.ui")
public class AiToolsFlowuiProperties {

    /**
     * Number of recent chats shown next to the chat input on the chat hub
     * screen. Can also be overridden per fragment via
     * {@code AiChatHubFragment.setRecentChatsCount(int)}.
     */
    int chatHubRecentChatsCount;

    /**
     * Maximum time an assistant response may take, counted from sending the
     * message (time waiting for a free generation thread included). On expiry
     * the response is cancelled and reported as failed.
     */
    Duration assistantResponseTimeout;

    /**
     * How the title of a new AI conversation is produced.
     */
    AiConversationTitleMode conversationTitleMode;

    /**
     * Number of threads that generate assistant responses. Responses beyond it wait in a queue.
     */
    int generationPoolSize;

    public AiToolsFlowuiProperties(@DefaultValue("6") int chatHubRecentChatsCount,
                                   @DefaultValue("5m") Duration assistantResponseTimeout,
                                   @DefaultValue("FIRST_MESSAGE") AiConversationTitleMode conversationTitleMode,
                                   @DefaultValue("10") int generationPoolSize) {
        this.chatHubRecentChatsCount = chatHubRecentChatsCount;
        this.assistantResponseTimeout = assistantResponseTimeout;
        this.conversationTitleMode = conversationTitleMode;
        this.generationPoolSize = generationPoolSize;
    }

    /**
     * @see #chatHubRecentChatsCount
     */
    public int getChatHubRecentChatsCount() {
        return chatHubRecentChatsCount;
    }

    /**
     * @see #assistantResponseTimeout
     */
    public Duration getAssistantResponseTimeout() {
        return assistantResponseTimeout;
    }

    /**
     * @see #conversationTitleMode
     */
    public AiConversationTitleMode getConversationTitleMode() {
        return conversationTitleMode;
    }

    /**
     * @see #generationPoolSize
     */
    public int getGenerationPoolSize() {
        return generationPoolSize;
    }
}

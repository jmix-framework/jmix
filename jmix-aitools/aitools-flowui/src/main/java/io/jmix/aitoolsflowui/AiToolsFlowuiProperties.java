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
     * Timeout for the background task that runs the LLM call producing the
     * assistant response. The task is cancelled and the failure handler is
     * invoked once the timeout elapses.
     */
    Duration assistantResponseTimeout;

    /**
     * How the title of a new AI conversation is produced.
     */
    AiConversationTitleMode conversationTitleMode;

    public AiToolsFlowuiProperties(@DefaultValue("6") int chatHubRecentChatsCount,
                                   @DefaultValue("5m") Duration assistantResponseTimeout,
                                   @DefaultValue("FIRST_MESSAGE") AiConversationTitleMode conversationTitleMode) {
        this.chatHubRecentChatsCount = chatHubRecentChatsCount;
        this.assistantResponseTimeout = assistantResponseTimeout;
        this.conversationTitleMode = conversationTitleMode;
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
}

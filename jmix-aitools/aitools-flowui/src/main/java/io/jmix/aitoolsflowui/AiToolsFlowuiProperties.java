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

    public AiToolsFlowuiProperties(@DefaultValue("6") int chatHubRecentChatsCount,
                                   @DefaultValue("5m") Duration assistantResponseTimeout) {
        this.chatHubRecentChatsCount = chatHubRecentChatsCount;
        this.assistantResponseTimeout = assistantResponseTimeout;
    }

    public int getChatHubRecentChatsCount() {
        return chatHubRecentChatsCount;
    }

    public Duration getAssistantResponseTimeout() {
        return assistantResponseTimeout;
    }
}

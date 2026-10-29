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

package io.jmix.aitoolsflowuidata;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties of the AI Tools FlowUI data module, bound from the
 * {@code jmix.aitools.ui.data} prefix.
 */
@ConfigurationProperties("jmix.aitools.ui.data")
public class AiToolsFlowuiDataProperties {

    /**
     * Whether the AI Tools FlowUI data autoconfiguration is enabled.
     */
    Boolean enabled;

    /**
     * Maximum number of most recent conversation messages kept as chat memory and sent with each request.
     * A non-positive value is treated as "no history": only the current user message is sent, so the
     * assistant is never invoked with an empty request.
     */
    Integer chatMemoryMaxMessages;

    /**
     * Maximum number of messages per conversation that can be kept in context, so that they are sent with every
     * request regardless of the {@link #chatMemoryMaxMessages} window. A non-positive value disables keeping:
     * every request to keep a message reports that the limit is reached. Lowering the value does not release
     * messages already kept, and all of them are still sent.
     */
    Integer keepInContextMaxMessages;

    public AiToolsFlowuiDataProperties(@DefaultValue("true") Boolean enabled,
                                       @DefaultValue("20") Integer chatMemoryMaxMessages,
                                       @DefaultValue("5") Integer keepInContextMaxMessages) {
        this.enabled = enabled;
        this.chatMemoryMaxMessages = chatMemoryMaxMessages;
        this.keepInContextMaxMessages = keepInContextMaxMessages;
    }

    /**
     * @see #enabled
     */
    public Boolean getEnabled() {
        return enabled;
    }

    /**
     * @see #chatMemoryMaxMessages
     */
    public Integer getChatMemoryMaxMessages() {
        return chatMemoryMaxMessages;
    }

    /**
     * @see #keepInContextMaxMessages
     */
    public Integer getKeepInContextMaxMessages() {
        return keepInContextMaxMessages;
    }
}

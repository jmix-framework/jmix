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

package io.jmix.aitools;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties of the AI Tools add-on, bound from the {@code aitools} prefix.
 */
@ConfigurationProperties("aitools")
public class AiToolsProperties {

    /**
     * Whether the AI Tools autoconfiguration is enabled.
     */
    Boolean enabled;

    /**
     * Maximum number of most recent conversation messages kept as chat memory and sent with each request.
     */
    Integer chatMemoryMaxMessages;

    public AiToolsProperties(@DefaultValue("true") Boolean enabled,
                             @DefaultValue("20") Integer chatMemoryMaxMessages) {
        this.enabled = enabled;
        this.chatMemoryMaxMessages = chatMemoryMaxMessages;
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
}

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

package io.jmix.autoconfigure.aitools;

import io.jmix.aitools.AiToolsConfiguration;
import io.jmix.aitools.memory.JmixChatMemoryRepository;
import io.jmix.core.CoreConfiguration;
import io.jmix.data.DataConfiguration;
import io.jmix.eclipselink.EclipselinkConfiguration;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnBooleanProperty(value = "aitools.enabled", matchIfMissing = true)
@Import({CoreConfiguration.class, AiToolsConfiguration.class, DataConfiguration.class, EclipselinkConfiguration.class})
public class AiToolsAutoConfiguration {

    @Bean("aitols_JmixChatMemoryRepository")
    @ConditionalOnMissingBean(ChatMemoryRepository.class)
    public ChatMemoryRepository jmixAiChatService() {
        return new JmixChatMemoryRepository();
    }
}

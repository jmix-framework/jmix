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

import io.jmix.aitools.dataload.prompt.DataLoadChatSystemPromptProvider;
import io.jmix.aitools.dataload.prompt.JpqlRepairerPromptProvider;
import io.jmix.aitools.dataload.prompt.impl.DefaultDataLoadChatSystemPromptProvider;
import io.jmix.aitools.dataload.prompt.impl.DefaultJpqlRepairerPromptProvider;
import io.jmix.aitools.dataload.repair.JpqlRepairer;
import io.jmix.aitools.dataload.repair.impl.DefaultJpqlRepairer;
import io.jmix.aitools.dataload.introspection.introspector.JpaDomainModelIntrospector;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnBooleanProperty(value = "aitools.dataload.enabled", matchIfMissing = true)
public class AiToolsDataLoadAutoConfiguration {

    @Bean("aitols_JpaDomainModelIntrospector")
    @ConditionalOnMissingBean
    public JpaDomainModelIntrospector jpaDomainModelIntrospector() {
        return new JpaDomainModelIntrospector();
    }

    @Bean("aitols_SpringAiJpqlRepairer")
    @ConditionalOnClass(ChatClient.class)
    @ConditionalOnMissingBean(JpqlRepairer.class)
    public JpqlRepairer textToJpqlRepairer() {
        return new DefaultJpqlRepairer();
    }

    @Bean("aitols_DefaultJpqlRepairerPromptProvider")
    @ConditionalOnMissingBean(JpqlRepairerPromptProvider.class)
    public JpqlRepairerPromptProvider jpqlRepairerPromptProvider() {
        return new DefaultJpqlRepairerPromptProvider();
    }

    @Bean("aitols_DefaultChatSystemPromptProvider")
    @ConditionalOnMissingBean(DataLoadChatSystemPromptProvider.class)
    public DataLoadChatSystemPromptProvider chatSystemPromptProvider() {
        return new DefaultDataLoadChatSystemPromptProvider();
    }
}

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

package io.jmix.autoconfigure.texttodata;

import io.jmix.texttodata.generation.impl.SpringAiJpqlGenerator;
import io.jmix.texttodata.prompt.SystemPromptProvider;
import io.jmix.texttodata.prompt.impl.DefaultJpqlGenerationPromptProvider;
import io.jmix.texttodata.prompt.impl.DefaultJpqlRepairerPromptProvider;
import io.jmix.texttodata.repair.impl.SpringAiJpqlRepairer;
import io.jmix.texttodata.TextToDataConfiguration;
import io.jmix.texttodata.generation.JpqlGenerator;
import io.jmix.texttodata.prompt.JpqlGenerationPromptProvider;
import io.jmix.texttodata.introspection.introspector.JpaDomainModelIntrospector;
import io.jmix.texttodata.prompt.JpqlRepairerPromptProvider;
import io.jmix.texttodata.prompt.impl.DefaultSystemPromptProvider;
import io.jmix.texttodata.repair.JpqlRepairer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@ConditionalOnBooleanProperty(value = "texttodata.enabled", matchIfMissing = true)
@Import(TextToDataConfiguration.class)
public class TextToDataAutoConfiguration {

    @Bean("textdt_JpaDomainModelIntrospector")
    @ConditionalOnMissingBean
    public JpaDomainModelIntrospector jpaDomainModelIntrospector() {
        return new JpaDomainModelIntrospector();
    }

    @Bean("textdt_SpringAiTextToJpqlGenerator")
    @ConditionalOnClass(ChatClient.class)
    @ConditionalOnBean(ChatClient.Builder.class)
    @ConditionalOnMissingBean(JpqlGenerator.class)
    public JpqlGenerator textToJpqlGenerator() {
        return new SpringAiJpqlGenerator();
    }

    @Bean("textdt_SpringAiTextToJpqlRepairer")
    @ConditionalOnClass(ChatClient.class)
    @ConditionalOnBean(ChatClient.Builder.class)
    @ConditionalOnMissingBean(JpqlRepairer.class)
    public JpqlRepairer textToJpqlRepairer() {
        return new SpringAiJpqlRepairer();
    }

    @Bean("textdt_DefaultJpqlRepairerPromptProvider")
    @ConditionalOnMissingBean(JpqlRepairerPromptProvider.class)
    public JpqlRepairerPromptProvider jpqlRepairerPromptProvider() {
        return new DefaultJpqlRepairerPromptProvider();
    }

    @Bean("textdt_JpqlGenerationPromptProvider")
    @ConditionalOnMissingBean(JpqlGenerationPromptProvider.class)
    public JpqlGenerationPromptProvider jpqlGenerationPromptProvider() {
        return new DefaultJpqlGenerationPromptProvider();
    }

    @Bean("textdt_DefaultSystemPromptProvider")
    @ConditionalOnMissingBean(SystemPromptProvider.class)
    public SystemPromptProvider systemPromptProvider() {
        return new DefaultSystemPromptProvider();
    }
}

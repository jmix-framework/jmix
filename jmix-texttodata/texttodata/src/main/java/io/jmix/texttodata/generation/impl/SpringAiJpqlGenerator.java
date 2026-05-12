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

package io.jmix.texttodata.generation.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jmix.texttodata.executor.SpringAiPromptExecutor;
import io.jmix.texttodata.generation.GeneratedJpqlResult;
import io.jmix.texttodata.generation.JpqlGenerationRequest;
import io.jmix.texttodata.generation.JpqlGenerator;
import io.jmix.texttodata.prompt.JpqlGenerationPromptProvider;
import io.jmix.texttodata.prompt.SystemPromptProvider;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

public class SpringAiJpqlGenerator implements JpqlGenerator, InitializingBean {

    @Autowired
    protected JpqlGenerationPromptProvider jpqlPromptProvider;
    @Autowired
    protected SystemPromptProvider systemPromptProvider;
    @Autowired
    protected ObjectProvider<ChatModel> chatModelProvider;

    protected SpringAiPromptExecutor promptExecutor;

    @Override
    public void afterPropertiesSet() {
        promptExecutor = createPromptExecutor();
    }

    @Override
    public GeneratedJpqlResult generate(JpqlGenerationRequest request) {
        String jpqlPrompt = jpqlPromptProvider.get();
        String formattedPrompt = jpqlPrompt.formatted(request.getUserText(), request.getPromptContext());

        return promptExecutor.executePrompt(formattedPrompt);
    }

    protected SpringAiPromptExecutor createPromptExecutor() {
        return new SpringAiPromptExecutor(chatModelProvider, createObjectMapper(), systemPromptProvider.get());
    }

    protected ObjectMapper createObjectMapper() {
        return new ObjectMapper();
    }
}

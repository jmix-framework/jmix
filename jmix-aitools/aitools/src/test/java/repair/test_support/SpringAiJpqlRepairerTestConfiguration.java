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

package repair.test_support;

import io.jmix.aitools.dataload.prompt.JpqlRepairerPromptProvider;
import io.jmix.aitools.dataload.prompt.DataLoadSystemPromptProvider;
import io.jmix.aitools.dataload.prompt.impl.DefaultJpqlRepairerPromptProvider;
import io.jmix.aitools.dataload.prompt.impl.DefaultDataLoadSystemPromptProvider;
import io.jmix.aitools.dataload.repair.impl.SpringAiJpqlRepairer;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiJpqlRepairerTestConfiguration {

    @Bean
    StubChatModel stubChatModel() {
        return new StubChatModel();
    }

    @Bean
    ChatClient.Builder chatClientBuilder(StubChatModel stubChatModel) {
        return ChatClient.builder(stubChatModel);
    }

    @Bean
    JpqlRepairerPromptProvider jpqlRepairerPromptProvider() {
        return new DefaultJpqlRepairerPromptProvider();
    }

    @Bean
    DataLoadSystemPromptProvider systemPromptProvider() {
        return new DefaultDataLoadSystemPromptProvider();
    }

    @Bean
    SpringAiJpqlRepairer springAiJpqlRepairer() {
        return new SpringAiJpqlRepairer();
    }
}

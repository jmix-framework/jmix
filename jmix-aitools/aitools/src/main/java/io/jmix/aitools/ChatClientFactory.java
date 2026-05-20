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

import io.jmix.core.common.util.Preconditions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component("aitols_ChatClientFactory")
public class ChatClientFactory {

    @Autowired
    protected ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    public ChatClient createChatClient() {
        return createBuilder().build();
    }

    public ChatClient createChatClient(Consumer<ChatClient.Builder> builderCustomizer) {
        Preconditions.checkNotNullArgument(builderCustomizer);

        ChatClient.Builder builder = createBuilder();
        builderCustomizer.accept(builder);
        return builder.build();
    }

    public ChatClient.Builder createBuilder() {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            throw new IllegalStateException(ChatClient.Builder.class.getSimpleName()
                    + " is not configured in application");
        }
        return builder.clone();
    }
}

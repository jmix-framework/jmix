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

import io.jmix.aitools.memory.ChatMemoryFactory;
import io.jmix.core.common.util.Preconditions;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallAdvisor;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component("aitols_ChatClientFactory")
public class ChatClientFactory {

    @Autowired
    protected ChatMemoryFactory chatMemoryFactory;
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

    public ChatClient createChatClientWithDefaultAdvisors() {
        return createChatClient(builder ->
                builder.defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        MessageChatMemoryAdvisor.builder(chatMemoryFactory.build())
                                .order(BaseAdvisor.HIGHEST_PRECEDENCE + 200)
                                .build(),
                        // Memory has the lower order, so it wraps the tool loop and runs once (not reloaded
                        // mid-iteration from the lossy repository). The tool advisor keeps its own conversation
                        // history across iterations (conversationHistoryEnabled=true, the default), so within a
                        // single exchange the live assistant(tool_calls) and tool responses stay intact and adjacent.
                        // (Spring logs a warning that memory is not updated between iterations — harmless here,
                        // since the tool advisor owns the in-loop history and memory persists once at the end.)
                        ToolCallAdvisor.builder()
                                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                                .build()));
    }

    /**
     * Creates a {@link ChatClient} with the same default advisors as
     * {@link #createChatClientWithDefaultAdvisors()} except for the
     * {@link MessageChatMemoryAdvisor}. Used by services that manage chat memory
     * themselves via {@link ChatClient.ChatClientRequestSpec#messages(java.util.List)}.
     */
    public ChatClient createChatClientWithoutMemoryAdvisor() {
        return createChatClient(builder ->
                builder.defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        ToolCallAdvisor.builder()
                                .advisorOrder(BaseAdvisor.HIGHEST_PRECEDENCE + 300)
                                .build()));
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

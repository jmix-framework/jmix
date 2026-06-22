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

import io.jmix.core.annotation.Internal;
import io.jmix.core.common.util.Preconditions;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.ToolCallingAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Class is a {@link ChatClient} factory. It creates instances from the application's configured
 * Spring AI model.
 */
@Internal
@NullMarked
@Component("aitls_ChatClientFactory")
public class ChatClientFactory implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ChatClientFactory.class);

    @Autowired
    protected ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    private ChatClient.@Nullable Builder builder;

    @Override
    public void afterPropertiesSet() {
        try {
            builder = chatClientBuilderProvider.getIfAvailable();
        } catch (BeansException e) {
            log.warn("Spring AI ChatClient.Builder is present but could not be created " +
                    "(e.g. no API key configured); treating AI as not configured: {}", e.getMessage());
        }
    }

    /**
     * Creates a simple chat client with no extra configurations.
     *
     * @return a new chat client
     * @throws IllegalStateException if no Spring AI model is configured
     */
    public ChatClient createChatClient() {
        return createBuilder().build();
    }

    /**
     * Creates a chat client, applying the given customizer to the builder before building.
     *
     * @param builderCustomizer customizer applied to the chat client builder
     * @return a new chat client
     * @throws IllegalStateException if no Spring AI model is configured
     */
    public ChatClient createChatClient(Consumer<ChatClient.Builder> builderCustomizer) {
        Preconditions.checkNotNullArgument(builderCustomizer);

        ChatClient.Builder clientBuilder = createBuilder();
        builderCustomizer.accept(clientBuilder);

        return clientBuilder.build();
    }

    /**
     * Creates a {@link ChatClient} with the default advisors, or an empty {@link Optional} when
     * Spring AI is not configured (see {@link #isConfigured()}).
     *
     * @return a new chat client with default advisors, or empty when Spring AI is not configured
     */
    public Optional<ChatClient> createChatClientWithDefaultAdvisors() {
        if (!isConfigured()) {
            return Optional.empty();
        }
        return Optional.of(createChatClient(clientBuilder ->
                clientBuilder.defaultAdvisors(
                        SimpleLoggerAdvisor.builder().build(),
                        ToolCallingAdvisor.builder().build())));
    }

    /**
     * Whether a usable {@link ChatClient.Builder} is available, i.e. the application has a configured
     * Spring AI model.
     *
     * @return {@code true} if a Spring AI model is configured
     */
    public boolean isConfigured() {
        return builder != null;
    }

    protected ChatClient.Builder createBuilder() {
        if (builder == null) {
            throw new IllegalStateException(ChatClient.Builder.class.getSimpleName()
                    + " is not configured in application");
        }
        return builder.clone();
    }
}

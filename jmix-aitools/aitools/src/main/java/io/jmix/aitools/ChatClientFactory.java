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

@Component("aitols_ChatClientFactory")
public class ChatClientFactory implements InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(ChatClientFactory.class);

    @Autowired
    protected ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    private ChatClient.@Nullable Builder builder;

    /**
     * Resolves the {@link ChatClient.Builder} bean once at startup. Leaves it {@code null} both when no
     * builder bean is declared and when it cannot be instantiated (e.g. a Spring AI model is on the
     * classpath but no API key is set — the bean factory then throws). Swallowing the creation failure
     * lets the application start and the UI degrade to read-only instead of failing at startup.
     */
    @Override
    public void afterPropertiesSet() {
        try {
            builder = chatClientBuilderProvider.getIfAvailable();
        } catch (BeansException e) {
            log.warn("Spring AI ChatClient.Builder is present but could not be created " +
                    "(e.g. no API key configured); treating AI as not configured: {}", e.getMessage());
        }
    }

    public ChatClient createChatClient() {
        return createBuilder().build();
    }

    public ChatClient createChatClient(Consumer<ChatClient.Builder> builderCustomizer) {
        Preconditions.checkNotNullArgument(builderCustomizer);

        ChatClient.Builder clientBuilder = createBuilder();
        builderCustomizer.accept(clientBuilder);
        return clientBuilder.build();
    }

    /**
     * Creates a {@link ChatClient} with the default advisors, or an empty {@link Optional} when
     * Spring AI is not configured (see {@link #isConfigured()}), so callers can degrade gracefully
     * instead of failing.
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

    public ChatClient.Builder createBuilder() {
        if (builder == null) {
            throw new IllegalStateException(ChatClient.Builder.class.getSimpleName()
                    + " is not configured in application");
        }
        return builder.clone();
    }

    /**
     * Whether a usable {@link ChatClient.Builder} is available, i.e. the application has a configured
     * Spring AI model. {@code false} both when no builder bean is declared and when it cannot be created
     * (e.g. a model is present but no API key is configured).
     */
    public boolean isConfigured() {
        return builder != null;
    }
}

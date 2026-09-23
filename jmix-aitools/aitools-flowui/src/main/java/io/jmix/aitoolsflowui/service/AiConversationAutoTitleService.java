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

package io.jmix.aitoolsflowui.service;

import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.flowui.asynctask.UiAsyncTasks;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Generates a conversation title in the background and persists it. The generated title is applied only
 * while the conversation still carries the title it was started with, so a manual rename is not overwritten.
 */
@Component("aitls_AiConversationAutoTitleService")
public class AiConversationAutoTitleService {

    private static final Logger log = LoggerFactory.getLogger(AiConversationAutoTitleService.class);

    @Autowired
    protected AiConversationTitleGenerator generator;
    @Autowired
    protected AiConversationService conversationService;
    @Autowired
    protected UiAsyncTasks uiAsyncTasks;

    /**
     * Runs {@link #generateAndApply(UUID, String, String)} in the background.
     * <p>
     * Must be called from a UI thread: the task is not cancelled when the user leaves the view,
     * and the callback runs on the UI thread.
     *
     * @param conversationId   id of the conversation to title
     * @param firstUserMessage first user message the title is derived from
     * @param expectedTitle    title the conversation must still carry for the generated title to be applied
     * @param onApplied        receives the applied title, or {@code null} when nothing was applied
     */
    public void generateAndApplyAsync(UUID conversationId, String firstUserMessage, String expectedTitle,
                                      Consumer<@Nullable String> onApplied) {
        uiAsyncTasks.supplierConfigurer(() -> generateAndApply(conversationId, firstUserMessage, expectedTitle))
                .withResultHandler(onApplied)
                .withExceptionHandler(e -> log.warn("Automatic title generation did not complete; " +
                        "the conversation keeps its first-message title", e))
                .supplyAsync();
    }

    /**
     * Generates the title and persists it if the conversation still carries {@code expectedTitle}.
     *
     * @param conversationId   id of the conversation to title
     * @param firstUserMessage first user message the title is derived from
     * @param expectedTitle    title the conversation must still carry
     * @return the applied title, or {@code null} when nothing was generated or the title changed meanwhile
     */
    @Nullable
    public String generateAndApply(UUID conversationId, String firstUserMessage, String expectedTitle) {
        String generated = generate(firstUserMessage);
        if (generated == null) {
            return null;
        }
        AiConversation conversation = conversationService.loadConversation(conversationId);
        if (conversation == null || !Objects.equals(conversation.getTitle(), expectedTitle)) {
            return null;
        }
        conversation.setTitle(generated);
        conversationService.save(conversation);
        return generated;
    }

    @Nullable
    protected String generate(String firstUserMessage) {
        try {
            return generator.generateTitle(firstUserMessage).orElse(null);
        } catch (RuntimeException e) {
            log.warn("Automatic title generation failed; keeping the first-message title", e);
            return null;
        }
    }
}

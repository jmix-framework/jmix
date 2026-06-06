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

import io.jmix.aitools.entity.AiConversation;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitools.service.AiConversationChatService;
import io.jmix.aitools.service.AiUiStatusUpdate;
import io.jmix.core.DataManager;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.view.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Runs the LLM call for a freshly persisted user {@link ChatMessage} on a
 * background task that streams ephemeral status updates and reports the
 * final assistant {@link ChatMessage} back to the UI.
 * <p>
 * Mirrors the CRM coordinator but invokes the add-on's
 * {@link AiConversationChatService} directly instead of going through a
 * project-specific analytics service.
 */
@Component("aitols_AssistantResponseTaskCoordinator")
public class AssistantResponseTaskCoordinator {

    private static final Logger log = LoggerFactory.getLogger(AssistantResponseTaskCoordinator.class);

    // TODO: pinyazhin — make timeout configurable via AiToolsProperties.
    private static final int TIMEOUT_MINUTES = 5;

    private final AiConversationChatService aiConversationChatService;
    private final BackgroundWorker backgroundWorker;
    private final DataManager dataManager;

    public AssistantResponseTaskCoordinator(AiConversationChatService aiConversationChatService,
                                            BackgroundWorker backgroundWorker,
                                            DataManager dataManager) {
        this.aiConversationChatService = aiConversationChatService;
        this.backgroundWorker = backgroundWorker;
        this.dataManager = dataManager;
    }

    /**
     * Submits an LLM call for {@code savedUserMessage}. The owner argument
     * scopes the background task to the lifecycle of the view that hosts the
     * fragment — when the view detaches, the task is cancelled.
     */
    public void run(View<?> owner,
                    AiConversation conversation,
                    ChatMessage savedUserMessage,
                    Consumer<AiUiStatusUpdate> progressHandler,
                    Consumer<ChatMessage> doneHandler,
                    Runnable failureHandler) {
        BackgroundTask<AiUiStatusUpdate, String> task = new AssistantResponseTask(
                owner, conversation, savedUserMessage, progressHandler, doneHandler, failureHandler
        );

        backgroundWorker.handle(task).execute();
    }

    private class AssistantResponseTask extends BackgroundTask<AiUiStatusUpdate, String> {
        private final AiConversation conversation;
        private final ChatMessage savedUserMessage;
        private final Consumer<AiUiStatusUpdate> progressHandler;
        private final Consumer<ChatMessage> doneHandler;
        private final Runnable failureHandler;

        AssistantResponseTask(View<?> owner,
                              AiConversation conversation,
                              ChatMessage savedUserMessage,
                              Consumer<AiUiStatusUpdate> progressHandler,
                              Consumer<ChatMessage> doneHandler,
                              Runnable failureHandler) {
            super(TIMEOUT_MINUTES, TimeUnit.MINUTES, owner);
            this.conversation = conversation;
            this.savedUserMessage = savedUserMessage;
            this.progressHandler = progressHandler;
            this.doneHandler = doneHandler;
            this.failureHandler = failureHandler;
        }

        @Override
        public String run(TaskLifeCycle<AiUiStatusUpdate> taskLifeCycle) {
            return aiConversationChatService.process(
                    savedUserMessage.getId(),
                    statusUpdate -> publishUiStatusUpdate(taskLifeCycle, statusUpdate)
            );
        }

        @Override
        public void progress(List<AiUiStatusUpdate> changes) {
            changes.forEach(progressHandler);
        }

        @Override
        public void done(String response) {
            doneHandler.accept(loadLatestAssistantMessage(conversation));
        }

        @Override
        public boolean handleException(Exception ex) {
            log.error("Error processing AI message async", ex);
            failureHandler.run();
            return true;
        }

        @Override
        public boolean handleTimeoutException() {
            log.error("Timed out while processing AI message {}", savedUserMessage.getId());
            failureHandler.run();
            return true;
        }
    }

    private void publishUiStatusUpdate(TaskLifeCycle<AiUiStatusUpdate> taskLifeCycle,
                                       AiUiStatusUpdate statusUpdate) {
        if (statusUpdate == null || taskLifeCycle.isInterrupted()) {
            return;
        }

        try {
            taskLifeCycle.publish(statusUpdate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI UI status update publishing was interrupted", e);
        }
    }

    private ChatMessage loadLatestAssistantMessage(AiConversation conversation) {
        return dataManager.load(ChatMessage.class)
                .query("e.conversation.id = :convId and e.type = :type order by e.createdDate desc, e.id desc")
                .parameter("convId", conversation.getId())
                .parameter("type", ChatMessageType.ASSISTANT.getId())
                .maxResults(1)
                .optional()
                .orElse(null);
    }
}

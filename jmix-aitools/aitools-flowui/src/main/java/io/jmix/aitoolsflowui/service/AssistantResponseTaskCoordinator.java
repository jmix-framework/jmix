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
import io.jmix.aitools.tool.AiUiStatusUpdate;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.core.DataManager;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
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

    protected final AiConversationChatService aiConversationChatService;
    protected final BackgroundWorker backgroundWorker;
    protected final DataManager dataManager;
    protected final AiToolsFlowuiProperties properties;

    public AssistantResponseTaskCoordinator(AiConversationChatService aiConversationChatService,
                                            BackgroundWorker backgroundWorker,
                                            DataManager dataManager,
                                            AiToolsFlowuiProperties properties) {
        this.aiConversationChatService = aiConversationChatService;
        this.backgroundWorker = backgroundWorker;
        this.dataManager = dataManager;
        this.properties = properties;
    }

    /**
     * Submits an LLM call for {@code savedUserMessage} on a background task.
     *
     * @param owner            view hosting the chat; scopes the task lifecycle (the task is cancelled when it detaches)
     * @param conversation     conversation the message belongs to
     * @param savedUserMessage already-persisted user message to answer
     * @param progressHandler  receives ephemeral status updates as they stream in
     * @param doneHandler      receives the final assistant message when the call completes
     * @param failureHandler   invoked on error or timeout
     */
    public void run(View<?> owner,
                    AiConversation conversation,
                    ChatMessage savedUserMessage,
                    Consumer<AiUiStatusUpdate> progressHandler,
                    Consumer<ChatMessage> doneHandler,
                    Runnable failureHandler) {
        BackgroundTask<AiUiStatusUpdate, String> task =
                new AssistantResponseTask(owner, conversation, savedUserMessage, doneHandler, failureHandler);
        task.addProgressListener(new BackgroundTask.ProgressListenerAdapter<>() {
            @Override
            public void onProgress(List<AiUiStatusUpdate> changes) {
                changes.forEach(progressHandler);
            }
        });
        backgroundWorker.handle(task).execute();
    }

    protected void publishUiStatusUpdate(TaskLifeCycle<AiUiStatusUpdate> taskLifeCycle,
                                         AiUiStatusUpdate statusUpdate) {
        if (taskLifeCycle.isInterrupted()) {
            return;
        }

        try {
            taskLifeCycle.publish(statusUpdate);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("AI UI status update publishing was interrupted", e);
        }
    }

    @Nullable
    private ChatMessage loadLatestAssistantMessage(AiConversation conversation) {
        return dataManager.load(ChatMessage.class)
                .query("e.conversation.id = :convId and e.type = :type order by e.createdDate desc, e.id desc")
                .parameter("convId", conversation.getId())
                .parameter("type", ChatMessageType.ASSISTANT.getId())
                .maxResults(1)
                .optional()
                .orElse(null);
    }

    protected class AssistantResponseTask extends BackgroundTask<AiUiStatusUpdate, String> {

        protected final AiConversation conversation;
        protected final ChatMessage savedUserMessage;
        protected final Consumer<@Nullable ChatMessage> doneHandler;
        protected final Runnable failureHandler;

        AssistantResponseTask(View<?> owner,
                              AiConversation conversation,
                              ChatMessage savedUserMessage,
                              Consumer<@Nullable ChatMessage> doneHandler,
                              Runnable failureHandler) {
            super(properties.getAssistantResponseTimeout().toSeconds(), owner);
            this.conversation = conversation;
            this.savedUserMessage = savedUserMessage;
            this.doneHandler = doneHandler;
            this.failureHandler = failureHandler;
        }

        @Nullable
        @Override
        public String run(TaskLifeCycle<AiUiStatusUpdate> taskLifeCycle) {
            return aiConversationChatService.process(
                    savedUserMessage.getId(),
                    statusUpdate -> publishUiStatusUpdate(taskLifeCycle, statusUpdate)
            );
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
}

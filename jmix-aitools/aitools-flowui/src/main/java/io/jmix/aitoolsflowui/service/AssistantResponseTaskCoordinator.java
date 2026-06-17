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

import io.jmix.aitools.tool.AiUiStatusUpdate;
import io.jmix.aitoolsflowui.AiToolsFlowuiProperties;
import io.jmix.aitoolsflowui.model.UserAiConversation;
import io.jmix.aitoolsflowui.model.UserAiMessage;
import io.jmix.aitoolsflowui.model.UserAiMessageType;
import io.jmix.flowui.backgroundtask.BackgroundTask;
import io.jmix.flowui.backgroundtask.BackgroundWorker;
import io.jmix.flowui.backgroundtask.TaskLifeCycle;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Runs the LLM call for a freshly persisted user {@link UserAiMessage} on a
 * background task that streams ephemeral status updates and reports the
 * final assistant {@link UserAiMessage} back to the UI.
 * <p>
 * Mirrors the CRM coordinator but invokes the add-on's
 * {@link UserAiChatService} directly instead of going through a
 * project-specific analytics service.
 */
@Component("aitls_AssistantResponseTaskCoordinator")
public class AssistantResponseTaskCoordinator {

    private static final Logger log = LoggerFactory.getLogger(AssistantResponseTaskCoordinator.class);

    @Autowired
    protected UserAiMessageService userAiMessageService;
    @Autowired
    protected UserAiChatService userAiChatService;
    @Autowired
    protected BackgroundWorker backgroundWorker;
    @Autowired
    protected AiToolsFlowuiProperties properties;

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
                    UserAiConversation conversation,
                    UserAiMessage savedUserMessage,
                    Consumer<AiUiStatusUpdate> progressHandler,
                    Consumer<UserAiMessage> doneHandler,
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
    private UserAiMessage loadLatestAssistantMessage(UserAiConversation conversation) {
        return userAiMessageService.loadLatestMessage(conversation, UserAiMessageType.ASSISTANT);
    }

    protected class AssistantResponseTask extends BackgroundTask<AiUiStatusUpdate, String> {

        protected final UserAiConversation conversation;
        protected final UserAiMessage message;
        protected final Consumer<@Nullable UserAiMessage> doneHandler;
        protected final Runnable failureHandler;

        AssistantResponseTask(View<?> owner,
                              UserAiConversation conversation,
                              UserAiMessage message,
                              Consumer<@Nullable UserAiMessage> doneHandler,
                              Runnable failureHandler) {
            super(properties.getAssistantResponseTimeout().toSeconds(), owner);
            this.conversation = conversation;
            this.message = message;
            this.doneHandler = doneHandler;
            this.failureHandler = failureHandler;
        }

        @Nullable
        @Override
        public String run(TaskLifeCycle<AiUiStatusUpdate> taskLifeCycle) {
            return userAiChatService.processMessage(message,
                    statusUpdate -> publishUiStatusUpdate(taskLifeCycle, statusUpdate));
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
            log.error("Timed out while processing AI message {}", message.getId());
            failureHandler.run();
            return true;
        }
    }
}

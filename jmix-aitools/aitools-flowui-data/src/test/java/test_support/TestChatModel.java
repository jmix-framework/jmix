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

package test_support;

import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import java.util.List;

/**
 * A chat model for tests that answers with a preset text or failure and records the last prompt.
 */
public class TestChatModel implements ChatModel {

    @Nullable
    protected volatile String content;
    @Nullable
    protected volatile RuntimeException failure;
    @Nullable
    protected volatile Prompt lastPrompt;

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    public void setFailure(@Nullable RuntimeException failure) {
        this.failure = failure;
    }

    @Nullable
    public Prompt getLastPrompt() {
        return lastPrompt;
    }

    /**
     * Forgets the preset answer, the failure and the recorded prompt, so that the next test starts clean.
     */
    public void reset() {
        content = null;
        failure = null;
        lastPrompt = null;
    }

    @Override
    public ChatOptions getOptions() {
        return ToolCallingChatOptions.builder().build();
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        lastPrompt = prompt;
        RuntimeException failure = this.failure;
        if (failure != null) {
            throw failure;
        }
        AssistantMessage message = AssistantMessage.builder().content(content).build();
        return ChatResponse.builder()
                .generations(List.of(new Generation(message)))
                .build();
    }
}

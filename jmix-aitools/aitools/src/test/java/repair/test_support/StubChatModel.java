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

import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import java.util.List;

public class StubChatModel implements ChatModel {

    protected String content;
    protected Prompt lastPrompt;

    public String getContent() {
        return content;
    }

    // The ToolCallAdvisor requires the prompt options to be ToolCallingChatOptions. A real model
    // (e.g. OpenAiChatModel) supplies them via its default options; the stub mirrors that so the
    // tool-calling advisor chain works under test.
    @Override
    public ChatOptions getDefaultOptions() {
        return ToolCallingChatOptions.builder().build();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Prompt getLastPrompt() {
        return lastPrompt;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        this.lastPrompt = prompt;
        return ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(content))))
                .build();
    }
}

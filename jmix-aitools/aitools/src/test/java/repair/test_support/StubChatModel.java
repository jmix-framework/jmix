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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class StubChatModel implements ChatModel {

    protected final Deque<ChatResponse> enqueuedResponses = new ArrayDeque<>();

    protected String content;
    protected Prompt lastPrompt;

    public String getContent() {
        return content;
    }

    @Override
    public ChatOptions getOptions() {
        return ToolCallingChatOptions.builder().build();
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Prompt getLastPrompt() {
        return lastPrompt;
    }

    /**
     * Enqueues a response asking the caller to call the given tool. Enqueued responses are returned
     * by {@link #call(Prompt)} before the plain {@link #getContent()} one, which lets a test drive a
     * tool-calling round trip.
     *
     * @param toolName  name of the tool the model asks for
     * @param arguments tool arguments as JSON
     */
    public void enqueueToolCall(String toolName, String arguments) {
        AssistantMessage message = AssistantMessage.builder()
                .content("")
                .toolCalls(List.of(new AssistantMessage.ToolCall(
                        "call-" + enqueuedResponses.size(), "function", toolName, arguments)))
                .build();

        enqueuedResponses.add(ChatResponse.builder()
                .generations(List.of(new Generation(message)))
                .build());
    }

    /**
     * Forgets what a test left behind. The stub is a bean of a context shared by several test classes, so a
     * response enqueued and never consumed would otherwise be answered to the next test that asks.
     */
    public void reset() {
        enqueuedResponses.clear();
        content = null;
        lastPrompt = null;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        this.lastPrompt = prompt;

        ChatResponse enqueuedResponse = enqueuedResponses.poll();
        if (enqueuedResponse != null) {
            return enqueuedResponse;
        }

        return ChatResponse.builder()
                .generations(List.of(new Generation(new AssistantMessage(content))))
                .build();
    }
}

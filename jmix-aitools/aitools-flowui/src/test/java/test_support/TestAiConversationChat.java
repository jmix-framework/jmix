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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.shared.Registration;
import io.jmix.aitoolsflowui.fragment.chat.AiConversationChat;
import io.jmix.aitoolsflowui.fragment.chat.AiConversationTitleChangeEvent;
import io.jmix.aitoolsflowui.fragment.chat.AiConversationTitleMode;
import io.jmix.aitoolsflowui.model.AiConversation;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A chat that only remembers what the host told it.
 */
public class TestAiConversationChat extends Div implements AiConversationChat {

    @Nullable
    AiConversation conversation;

    @Override
    public void setConversation(@Nullable AiConversation conversation) {
        this.conversation = conversation;
    }

    @Override
    public void setConversationId(@Nullable UUID conversationId) {
    }

    @Override
    public void sendMessage(@Nullable String userMessage) {
    }

    @Override
    public void focusMessageInput() {
    }

    @Override
    public void setMessageInputEnabled(boolean enabled) {
    }

    @Override
    public void setReadOnly(boolean readOnly) {
    }

    @Override
    public boolean isAwaitingResponse() {
        return false;
    }

    @Override
    public void setTitleVisible(boolean titleVisible) {
    }

    @Override
    public void setTitleMode(@Nullable AiConversationTitleMode titleMode) {
    }

    @Override
    public Registration addTitleChangeListener(ComponentEventListener<AiConversationTitleChangeEvent> listener) {
        return addListener(AiConversationTitleChangeEvent.class, listener);
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Nullable
    public AiConversation getConversation() {
        return conversation;
    }
}

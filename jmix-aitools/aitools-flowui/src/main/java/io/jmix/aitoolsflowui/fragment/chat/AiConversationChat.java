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

package io.jmix.aitoolsflowui.fragment.chat;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.shared.Registration;
import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * A chat UI for one {@link AiConversation}: shows its messages and the assistant response running in it, and lets the
 * user send messages. A host talks to the chat only through this contract, so the way the chat is rendered can be
 * replaced without changing the host.
 */
@Experimental
@NullMarked
public interface AiConversationChat {

    /**
     * Binds a conversation to the chat.
     *
     * @param conversation conversation to show, or {@code null} to clear the chat
     */
    void setConversation(@Nullable AiConversation conversation);

    /**
     * Binds a conversation to the chat by id.
     *
     * @param conversationId id of the conversation to show; {@code null} or an id that does not resolve clears the chat
     */
    void setConversationId(@Nullable UUID conversationId);

    /**
     * Sends a user message to the bound conversation and starts the assistant's response. Ignored when the message
     * cannot be sent: no conversation is bound, the text is blank, a response is already shown as running, or the AI
     * is not available.
     *
     * @param userMessage message text to send; a {@code null} or blank value is ignored
     */
    void sendMessage(@Nullable String userMessage);

    /**
     * Hides the message input and the conversation rename control.
     *
     * @param readOnly {@code true} to hide them
     */
    void setReadOnly(boolean readOnly);

    /**
     * Returns whether the chat shows an assistant response as running in the bound conversation.
     *
     * @return {@code true} while an assistant response is shown as running
     */
    boolean isAwaitingResponse();

    /**
     * Shows or hides the conversation title, for a host that names the conversation in its own chrome. The rename
     * control stays, and {@link AiConversationTitleChangeEvent} fires either way.
     *
     * @param titleVisible {@code false} to hide the title; visible by default
     */
    void setTitleVisible(boolean titleVisible);

    /**
     * Overrides the application-level {@code jmix.aitools.ui.conversation-title-mode} for this chat.
     *
     * @param titleMode the title mode for this chat, or {@code null} to use the application-level setting
     */
    void setTitleMode(@Nullable AiConversationTitleMode titleMode);

    /**
     * Adds a listener notified whenever the title of the shown conversation changes.
     *
     * @param listener listener to add
     * @return a registration for removing the listener
     */
    Registration addTitleChangeListener(ComponentEventListener<AiConversationTitleChangeEvent> listener);

    /**
     * Returns the chat as a component to place into a layout.
     *
     * @return the chat component
     */
    Component asComponent();
}

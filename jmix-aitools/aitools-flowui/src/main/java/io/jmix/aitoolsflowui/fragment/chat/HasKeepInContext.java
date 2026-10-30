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

import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;

/**
 * An {@link AiChatPanel} that shows which messages are kept in the assistant's context and lets the user keep or
 * release a message. The panel only reports the user's request; the host stores the flag and calls
 * {@link #refreshKeepInContext(AiChatMessage)} to show the outcome.
 * <p>
 * The control is offered only while a handler is set: the host clears the handler when the chat must not change
 * what the assistant remembers, for example in a read-only chat.
 */
@Experimental
@NullMarked
public interface HasKeepInContext {

    /**
     * Sets the handler that the panel calls when the user asks to keep or release a message. Without a handler the
     * panel offers no such control.
     *
     * @param handler the handler, or {@code null} to remove it
     */
    void setKeepInContextHandler(@Nullable KeepInContextHandler handler);

    /**
     * Redraws the keep mark of a message from its current {@link AiChatMessage#getKeepInContext()} value, without
     * reloading the conversation or moving the scroll position. Does nothing if the message is not shown.
     *
     * @param message message whose mark changed
     */
    void refreshKeepInContext(AiChatMessage message);

    /**
     * Brings a message into view. Does nothing if the message is not shown.
     *
     * @param message message to show
     */
    void scrollToMessage(AiChatMessage message);

    /**
     * Receives the user's request to keep a message in the assistant's context or to release it.
     */
    @FunctionalInterface
    interface KeepInContextHandler extends Serializable {

        /**
         * Called when the user asks to change the keep mark of a message.
         *
         * @param message message the user acted on
         * @param keep    {@code true} to keep the message, {@code false} to release it
         */
        void onKeepInContextRequested(AiChatMessage message, boolean keep);
    }
}

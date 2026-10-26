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
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * What {@link AiChatFragment} draws a conversation with: the messages, the running response and the message input.
 * The fragment owns the behaviour and calls these methods; the panel reports the user's actions through the handlers.
 * Hosts do not use panels directly: {@link AiChatFragment} obtains one from {@link AiChatPanelFactory}.
 */
@Experimental
@NullMarked
public interface AiChatPanel {

    /**
     * Returns the panel as a component to place into the fragment.
     *
     * @return the panel component
     */
    Component asComponent();

    /**
     * Sets the handler of a message the user submits. The panel decides itself what to do with the input text when
     * the message is rejected, for example puts it back.
     *
     * @param handler receives the submitted message and tells whether it was taken
     */
    void setSubmitHandler(Function<SubmitContext, SubmitResult> handler);

    /**
     * Sets the handler of the user's Stop of the running response. A panel without a Stop control never calls it.
     *
     * @param handler called when the user stops the response
     */
    void setStopHandler(Runnable handler);

    /**
     * Sets the handler of the user's Retry of a failed response. A panel without a Retry control never calls it.
     *
     * @param handler called when the user retries
     */
    void setRetryHandler(Runnable handler);

    /**
     * Moves keyboard focus to the input. Does nothing while the input is disabled.
     */
    void focusInput();

    /**
     * Enables or disables the message input.
     *
     * @param enabled {@code false} to disable it
     */
    void setInputEnabled(boolean enabled);

    /**
     * Shows or hides the message input.
     *
     * @param visible {@code false} to hide it
     */
    void setInputVisible(boolean visible);

    /**
     * Whether the fragment disables the input while a response runs. A panel whose input stays usable then (e.g. to
     * stop the response) and blocks sending by itself returns {@code false}.
     *
     * @return {@code true} to have the input disabled while a response runs
     */
    default boolean isInputLockedWhileResponding() {
        return true;
    }

    /**
     * Shows the given messages in place of everything shown. Called only after the running response, if any, stopped
     * being shown.
     *
     * @param messages messages of the conversation, oldest first
     */
    void showMessages(Collection<AiChatMessage> messages);

    /**
     * Adds a user message just sent after the shown messages.
     *
     * @param message the saved user message
     */
    void appendUserMessage(AiChatMessage message);

    /**
     * Shows the response as running after the shown messages, without statuses yet.
     */
    void showResponseRunning();

    /**
     * Shows the tool statuses of the running response.
     *
     * @param statuses the folded statuses, oldest first
     */
    void showStatuses(List<AiToolStatusUpdate> statuses);

    /**
     * Stops showing the response as running.
     */
    void hideResponseRunning();

    /**
     * Tells the user that the response failed. Called after the running response is no longer shown.
     *
     * @param errorText the localized text to show
     */
    void showResponseFailed(String errorText);

    /**
     * Called after a completed response's answer has been shown with the other messages.
     *
     * @param assistantMessageId id of the persisted answer
     */
    void onResponseCompleted(UUID assistantMessageId);

    /**
     * A message the user submits in the panel.
     */
    final class SubmitContext {

        private final String text;

        private SubmitContext(String text) {
            this.text = text;
        }

        /**
         * Creates the context of a submitted message.
         *
         * @param text the trimmed, non-blank text of the message
         * @return the context
         */
        public static SubmitContext of(String text) {
            return new SubmitContext(text);
        }

        /**
         * Returns the text of the message.
         *
         * @return the trimmed, non-blank text
         */
        public String getText() {
            return text;
        }
    }

    /**
     * Whether a submitted message was taken. A rejected message is not sent, so its text is still the user's.
     */
    final class SubmitResult {

        private static final SubmitResult ACCEPTED = new SubmitResult(true);
        private static final SubmitResult REJECTED = new SubmitResult(false);

        private final boolean accepted;

        private SubmitResult(boolean accepted) {
            this.accepted = accepted;
        }

        /**
         * Returns the result of a message that was sent.
         *
         * @return the accepted result
         */
        public static SubmitResult accepted() {
            return ACCEPTED;
        }

        /**
         * Returns the result of a message that was not sent.
         *
         * @return the rejected result
         */
        public static SubmitResult rejected() {
            return REJECTED;
        }

        /**
         * Returns whether the message was sent.
         *
         * @return {@code true} if the message was sent
         */
        public boolean isAccepted() {
            return accepted;
        }
    }
}

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

package io.jmix.aitoolsflowui.view.input;

import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitoolsflowui.view.chat.AiChatFragment;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.ViewComponent;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Reusable chat input ("composer"). Replaces the stock Vaadin {@link MessageInput} with a
 * {@link JmixTextArea} + send {@link JmixButton}, so it can be embedded both in
 * the timeline ({@link AiChatFragment}) and on the chat hub screen.
 * <p>
 * <b>Submit gesture.</b> Enter submits; Shift+Enter inserts a newline. The
 * server-side submit fires only for a bare Enter (DOM keydown filter) and the
 * newline insertion is suppressed client-side via {@code preventDefault}.
 */
@Experimental
@FragmentDescriptor("ai-chat-input-fragment.xml")
public class AiChatInputFragment extends Fragment<VerticalLayout> {

    @ViewComponent
    protected JmixTextArea messageField;
    @ViewComponent
    protected JmixButton sendButton;

    @Nullable
    protected Consumer<String> submitHandler;

    public AiChatInputFragment() {
        addReadyListener(this::onReady);
    }

    /**
     * Sets the handler invoked with the trimmed, non-blank message text when the
     * user submits. Passing {@code null} restores a no-op handler.
     *
     * @param submitHandler handler invoked with the submitted text, or {@code null} for a no-op
     */
    public void setSubmitHandler(@Nullable Consumer<String> submitHandler) {
        this.submitHandler = submitHandler;
    }

    /**
     * Sets the placeholder text of the input.
     *
     * @param placeholder placeholder text, or {@code null} for none
     */
    public void setPlaceholder(@Nullable String placeholder) {
        messageField.setPlaceholder(placeholder);
    }

    /**
     * Enables/disables the input and the send button together.
     *
     * @param enabled {@code true} to enable the input and the send button
     */
    public void setInputEnabled(boolean enabled) {
        messageField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    /**
     * Returns whether the input is enabled.
     *
     * @return {@code true} if the input is enabled
     */
    public boolean isInputEnabled() {
        return messageField.isEnabled();
    }

    /**
     * Moves keyboard focus to the input. No-op if the input is disabled.
     */
    public void focus() {
        if (messageField.isEnabled()) {
            messageField.focus();
        }
    }

    /**
     * Clears the input text.
     */
    public void clear() {
        messageField.clear();
    }

    /**
     * Returns the current input text.
     *
     * @return current text (empty string if nothing was entered)
     */
    public String getValue() {
        return messageField.getValue();
    }

    protected void onReady(ReadyEvent event) {
        // Enter submits, Shift+Enter newline. The filter limits the server event
        // to a bare Enter; addEventData evaluates event.preventDefault() on the
        // client (only when the filter matched), suppressing the inserted newline.
        messageField.getElement()
                .addEventListener("keydown", e -> submit())
                .setFilter("event.key === 'Enter' && !event.shiftKey && !event.isComposing")
                .addEventData("event.preventDefault()");

        sendButton.addClickListener(e -> submit());
    }

    protected void submit() {
        if (!messageField.isEnabled()) {
            return;
        }
        String text = messageField.getValue();
        if (text == null || text.isBlank()) {
            return;
        }
        if (submitHandler != null) {
            submitHandler.accept(text.trim());
        }
    }
}

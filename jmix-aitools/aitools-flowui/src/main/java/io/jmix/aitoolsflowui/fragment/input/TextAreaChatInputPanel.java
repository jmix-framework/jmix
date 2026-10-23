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

package io.jmix.aitoolsflowui.fragment.input;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.component.textarea.JmixTextArea;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.kit.component.button.JmixButton;
import io.jmix.flowui.view.ViewComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * The add-on's {@link AiChatInputPanel}: a text area with a send button. The text area clears itself on submit.
 * <p>
 * <b>Submit gesture.</b> Enter submits; Shift+Enter inserts a newline. The
 * server-side submit fires only for a bare Enter (DOM keydown filter) and the
 * newline insertion is suppressed client-side via {@code preventDefault}.
 */
@Experimental
@NullMarked
@FragmentDescriptor("text-area-chat-input-panel.xml")
public class TextAreaChatInputPanel extends Fragment<VerticalLayout> implements AiChatInputPanel {

    @ViewComponent
    protected JmixTextArea messageField;
    @ViewComponent
    protected JmixButton sendButton;

    @Nullable
    protected Consumer<String> submitHandler;

    public TextAreaChatInputPanel() {
        addReadyListener(this::onReady);
    }

    /**
     * Sets the handler invoked with the trimmed, non-blank message text when the
     * user submits; the input is already cleared by then. Passing {@code null} restores a no-op handler.
     *
     * @param submitHandler handler invoked with the submitted text, or {@code null} for a no-op
     */
    @Override
    public void setSubmitHandler(@Nullable Consumer<String> submitHandler) {
        this.submitHandler = submitHandler;
    }

    /**
     * Sets the placeholder text of the input.
     *
     * @param placeholder placeholder text, or {@code null} for none
     */
    @Override
    public void setPlaceholder(@Nullable String placeholder) {
        messageField.setPlaceholder(placeholder);
    }

    /**
     * Enables/disables the input and the send button together.
     *
     * @param enabled {@code true} to enable the input and the send button
     */
    @Override
    public void setInputEnabled(boolean enabled) {
        messageField.setEnabled(enabled);
        sendButton.setEnabled(enabled);
    }

    /**
     * Returns whether the input is enabled.
     *
     * @return {@code true} if the input is enabled
     */
    @Override
    public boolean isInputEnabled() {
        return messageField.isEnabled();
    }

    /**
     * Moves keyboard focus to the input. No-op if the input is disabled.
     */
    @Override
    public void focus() {
        if (messageField.isEnabled()) {
            messageField.focus();
        }
    }

    @Override
    public void restoreText(String text) {
        messageField.setValue(text);
    }

    @Override
    public Component asComponent() {
        return this;
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
        // Cleared before the handler: a host that cannot take the message puts the text back from inside the
        // handler (restoreText), so clearing afterwards would wipe it.
        messageField.clear();
        if (submitHandler != null) {
            submitHandler.accept(text.trim());
        }
    }
}

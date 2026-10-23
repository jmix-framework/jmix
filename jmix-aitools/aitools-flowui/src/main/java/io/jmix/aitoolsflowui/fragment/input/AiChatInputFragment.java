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
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelFactory;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanelProvider;
import io.jmix.core.annotation.Experimental;
import io.jmix.flowui.fragment.Fragment;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.view.Subscribe;
import io.jmix.flowui.view.ViewComponent;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Consumer;

/**
 * The message input of the AI chat: embed it in a view or fragment. What it draws comes from the first
 * {@link AiChatPanelProvider} by order — the add-on's text area, or the input of another add-on.
 * <p>
 * The input clears itself when the user submits, before the submit handler runs.
 */
@Experimental
@NullMarked
@FragmentDescriptor("ai-chat-input-fragment.xml")
public class AiChatInputFragment extends Fragment<VerticalLayout> {

    @Autowired
    protected AiChatPanelFactory chatPanelFactory;

    @ViewComponent
    protected VerticalLayout inputPanelContainer;

    @Nullable
    protected AiChatInputPanel inputPanel;

    /**
     * Draws the panel as soon as the fragment is ready, so a fragment declared in XML shows its input even if the
     * host never calls it.
     *
     * @param event the ready event
     */
    @Subscribe
    public void onReady(ReadyEvent event) {
        getInputPanel();
    }

    /**
     * Sets the handler invoked with the trimmed, non-blank message text when the user submits.
     *
     * @param submitHandler handler invoked with the submitted text, or {@code null} for a no-op
     */
    public void setSubmitHandler(@Nullable Consumer<String> submitHandler) {
        getInputPanel().setSubmitHandler(submitHandler);
    }

    /**
     * Sets the placeholder text of the input.
     *
     * @param placeholder placeholder text, or {@code null} for none
     */
    public void setPlaceholder(@Nullable String placeholder) {
        getInputPanel().setPlaceholder(placeholder);
    }

    /**
     * Enables or disables typing and submitting.
     *
     * @param enabled {@code false} to disable the input
     */
    public void setInputEnabled(boolean enabled) {
        getInputPanel().setInputEnabled(enabled);
    }

    /**
     * Returns whether the input is enabled.
     *
     * @return {@code true} if the input is enabled
     */
    public boolean isInputEnabled() {
        return getInputPanel().isInputEnabled();
    }

    /**
     * Moves keyboard focus to the input; no-op while the input is disabled.
     */
    public void focus() {
        getInputPanel().focus();
    }

    /**
     * Puts back the text of a submit the host could not take. The input clears itself when the user submits, so the
     * host never clears it.
     *
     * @param text the text to show
     */
    public void restoreText(String text) {
        getInputPanel().restoreText(text);
    }

    /**
     * Returns the panel the input is drawn with, creating it on first use.
     *
     * @return the panel
     */
    protected AiChatInputPanel getInputPanel() {
        if (inputPanel == null) {
            inputPanel = chatPanelFactory.createInputPanel(this);
            Component inputPanelComponent = inputPanel.asComponent();
            if (inputPanelComponent instanceof HasSize sized) {
                sized.setWidthFull();
            }
            inputPanelContainer.add(inputPanelComponent);
        }
        return inputPanel;
    }
}

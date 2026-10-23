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
import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

/**
 * What {@link AiChatInputFragment} draws its message input with. Hosts use the fragment; it obtains the panel from
 * {@link io.jmix.aitoolsflowui.fragment.chat.AiChatPanelFactory}.
 */
@Experimental
@NullMarked
public interface AiChatInputPanel {

    /**
     * Returns the panel as a component to place into the fragment.
     *
     * @return the panel component
     */
    Component asComponent();

    /**
     * Sets the handler of a submitted message.
     *
     * @param submitHandler receives the trimmed, non-blank text, or {@code null} for none
     */
    void setSubmitHandler(@Nullable Consumer<String> submitHandler);

    /**
     * Sets the placeholder text of the input.
     *
     * @param placeholder placeholder text, or {@code null} for none
     */
    void setPlaceholder(@Nullable String placeholder);

    /**
     * Enables or disables typing and submitting.
     *
     * @param enabled {@code false} to disable the input
     */
    void setInputEnabled(boolean enabled);

    /**
     * Returns whether the input is enabled.
     *
     * @return {@code true} if the input is enabled
     */
    boolean isInputEnabled();

    /**
     * Moves keyboard focus to the input; no-op while the input is disabled.
     */
    void focus();

    void clear();

    /**
     * Puts back the text of a submit the host could not take.
     *
     * @param text the text to show
     */
    void restoreText(String text);

    /**
     * Returns the current text.
     *
     * @return the text, empty if none
     */
    String getValue();
}

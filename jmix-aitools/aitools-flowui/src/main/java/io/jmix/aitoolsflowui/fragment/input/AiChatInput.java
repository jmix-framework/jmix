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
 * A message input that is not bound to a conversation, such as the one the chat hub starts a conversation with.
 * A host talks to the input only through this contract, so the way the input is rendered can be replaced together
 * with the conversation chat.
 */
@Experimental
@NullMarked
public interface AiChatInput {

    /**
     * Sets the handler of a submitted message.
     *
     * @param submitHandler receives the text of a non-blank submitted message, or {@code null} to ignore submits
     */
    void setSubmitHandler(@Nullable Consumer<String> submitHandler);

    /**
     * Moves keyboard focus to the input.
     */
    void focus();

    /**
     * Clears the text of the input.
     */
    void clear();

    /**
     * Puts the text of a message that could not be handled back into the input.
     *
     * @param text the text of the message
     */
    void restoreText(String text);

    /**
     * Enables or disables the input.
     *
     * @param enabled {@code false} to disable the input
     */
    void setInputEnabled(boolean enabled);

    /**
     * Returns the input as a component to place into a layout.
     *
     * @return the input component
     */
    Component asComponent();
}

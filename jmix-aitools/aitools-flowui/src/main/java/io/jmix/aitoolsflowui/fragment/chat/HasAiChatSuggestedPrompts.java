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

import com.vaadin.flow.function.SerializableConsumer;
import io.jmix.core.annotation.Experimental;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * An {@link AiChatPanel} that offers the user ready questions below the message input. The panel only reports the
 * question the user picks. The host decides which questions to offer and what picking one does.
 */
@Experimental
@NullMarked
public interface HasAiChatSuggestedPrompts {

    /**
     * Shows the questions to offer, replacing the ones shown before.
     *
     * @param prompts questions to offer, or an empty list to show none
     */
    void setSuggestedPrompts(List<String> prompts);

    /**
     * Sets the handler that the panel calls with the text of the question the user picks.
     *
     * @param handler the handler, or {@code null} to remove it
     */
    void setSuggestedPromptHandler(@Nullable SerializableConsumer<String> handler);
}

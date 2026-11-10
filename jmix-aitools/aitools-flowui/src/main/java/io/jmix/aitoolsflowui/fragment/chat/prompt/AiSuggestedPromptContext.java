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

package io.jmix.aitoolsflowui.fragment.chat.prompt;

import io.jmix.aitoolsflowui.model.AiConversation;
import io.jmix.core.annotation.Experimental;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The state of a chat that questions are suggested for.
 */
@Experimental
@NullMarked
public class AiSuggestedPromptContext {

    @Nullable
    private final AiConversation conversation;
    @Nullable
    private final MetaClass dataMetaClass;
    @Nullable
    private final View<?> view;

    /**
     * Creates a context.
     *
     * @param conversation  conversation of the chat, or {@code null} if the first message has not created it yet
     * @param dataMetaClass entity of the data that goes with the user's next question, or {@code null} if there is
     *                      no data or its entity is unknown
     * @param view          view the data comes from, or {@code null} if there is no data or the view is unknown
     */
    public AiSuggestedPromptContext(@Nullable AiConversation conversation,
                                    @Nullable MetaClass dataMetaClass,
                                    @Nullable View<?> view) {
        this.conversation = conversation;
        this.dataMetaClass = dataMetaClass;
        this.view = view;
    }

    /**
     * Returns the conversation of the chat.
     *
     * @return the conversation, or {@code null} if the first message has not created it yet
     */
    @Nullable
    public AiConversation getConversation() {
        return conversation;
    }

    /**
     * Returns the entity of the data that goes with the user's next question, e.g. rows the user wants to ask about.
     *
     * @return the entity, or {@code null} if there is no data or its entity is unknown
     */
    @Nullable
    public MetaClass getDataMetaClass() {
        return dataMetaClass;
    }

    /**
     * Returns the view the data that goes with the user's next question comes from, e.g. the list view whose rows
     * the user wants to ask about. Together with {@link #getDataMetaClass()} it tells which questions fit.
     * <p>
     * A provider may be called outside the UI thread, and the view may already be closed if the chat outlives it, so
     * match the view by its class rather than reading its state.
     *
     * @return the view, or {@code null} if there is no data or the view is unknown
     */
    @Nullable
    public View<?> getView() {
        return view;
    }
}

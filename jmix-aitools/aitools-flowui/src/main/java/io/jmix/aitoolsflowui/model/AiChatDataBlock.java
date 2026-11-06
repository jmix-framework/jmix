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

package io.jmix.aitoolsflowui.model;

import io.jmix.aitoolsflowui.prompt.AiSuggestedPromptProvider;
import io.jmix.core.annotation.Internal;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.view.View;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.Serializable;
import java.util.List;

import static io.jmix.core.common.util.Preconditions.checkNotNullArgument;

/**
 * INTERNAL.
 * <p>
 * A block of data waiting in an open chat to be sent with the user's next question. It exists only while the chat
 * is open: nothing is stored until the user sends it.
 */
@Internal
@NullMarked
public class AiChatDataBlock implements Serializable {

    private final String caption;
    private final String content;
    @Nullable
    private final MetaClass metaClass;
    @Nullable
    private final View<?> view;
    private final List<String> suggestedPrompts;

    /**
     * Creates a data block of unknown origin for which the chat chooses the prompts to offer.
     *
     * @param caption   short text the chat shows for the pending block, e.g. "Orders · 12 rows"
     * @param content   Markdown appended to the user's question when it is sent
     * @param metaClass entity of the rows, or {@code null} if unknown
     */
    public AiChatDataBlock(String caption, String content, @Nullable MetaClass metaClass) {
        this(caption, content, metaClass, null, List.of());
    }

    /**
     * Creates a data block.
     *
     * @param caption          short text the chat shows for the pending block, e.g. "Orders · 12 rows"
     * @param content          Markdown appended to the user's question when it is sent
     * @param metaClass        entity of the rows, or {@code null} if unknown
     * @param view             view the rows come from, or {@code null} if unknown
     * @param suggestedPrompts prompts to offer for this block, or an empty list to let the chat choose them
     */
    public AiChatDataBlock(String caption,
                           String content,
                           @Nullable MetaClass metaClass,
                           @Nullable View<?> view,
                           List<String> suggestedPrompts) {
        checkNotNullArgument(caption);
        checkNotNullArgument(content);
        checkNotNullArgument(suggestedPrompts);

        this.caption = caption;
        this.content = content;
        this.metaClass = metaClass;
        this.view = view;
        this.suggestedPrompts = List.copyOf(suggestedPrompts);
    }

    /**
     * Returns the text the chat shows for the pending block.
     *
     * @return the caption
     */
    public String getCaption() {
        return caption;
    }

    /**
     * Returns the data sent with the user's question.
     *
     * @return Markdown appended to the question
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the entity of the rows in the block.
     *
     * @return the entity, or {@code null} if unknown
     */
    @Nullable
    public MetaClass getMetaClass() {
        return metaClass;
    }

    /**
     * Returns the view the rows in the block come from. A chat that outlives the view keeps it, so the view may
     * already be closed.
     *
     * @return the view, or {@code null} if unknown
     */
    @Nullable
    public View<?> getView() {
        return view;
    }

    /**
     * Returns the prompts the caller chose for this block. When not empty, the chat offers them instead of the
     * suggested ones ({@link AiSuggestedPromptProvider}).
     *
     * @return the prompts, or an empty list if the caller chose none
     */
    public List<String> getSuggestedPrompts() {
        return suggestedPrompts;
    }
}

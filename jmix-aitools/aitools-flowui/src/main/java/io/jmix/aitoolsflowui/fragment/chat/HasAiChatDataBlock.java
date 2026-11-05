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

import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.core.annotation.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * INTERNAL.
 * <p>
 * An {@link AiChatPanel} that shows a block of data waiting to be sent with the user's next question, above the
 * message input. The panel only reports that the user removes the block; the host keeps the block and tells the
 * panel what to show.
 */
@Internal
@NullMarked
public interface HasAiChatDataBlock {

    /**
     * Shows the pending data block above the message input, replacing the one shown before. The user can remove
     * the block, which the panel reports to the {@link #setDataBlockRemoveHandler(SerializableRunnable) handler}.
     *
     * @param caption short text that names the data
     * @param warning localized text shown with the block, or {@code null} to show none
     */
    void showDataBlock(String caption, @Nullable String warning);

    /**
     * Hides the data block.
     */
    void hideDataBlock();

    /**
     * Sets the handler that the panel calls when the user removes the data block.
     *
     * @param handler the handler, or {@code null} to remove it
     */
    void setDataBlockRemoveHandler(@Nullable SerializableRunnable handler);
}

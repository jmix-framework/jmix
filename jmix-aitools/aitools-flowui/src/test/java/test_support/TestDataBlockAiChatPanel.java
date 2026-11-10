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

package test_support;

import com.vaadin.flow.function.SerializableConsumer;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.aitoolsflowui.fragment.chat.HasAiChatSuggestedPrompts;
import io.jmix.aitoolsflowui.fragment.chat.datablock.HasAiChatDataBlock;
import org.jspecify.annotations.Nullable;

import java.util.List;

/**
 * A recording chat panel that also shows the assistant memory marks, the pending data block and the suggested
 * prompts.
 */
public class TestDataBlockAiChatPanel extends TestKeepInContextAiChatPanel implements HasAiChatDataBlock,
        HasAiChatSuggestedPrompts {

    public boolean dataBlockShown;
    @Nullable
    public String dataBlockCaption;
    @Nullable
    public String dataBlockWarning;
    @Nullable
    public SerializableRunnable dataBlockRemoveHandler;
    public List<String> suggestedPrompts = List.of();
    @Nullable
    public SerializableConsumer<String> suggestedPromptHandler;

    /**
     * Removes the data block as the user would with its remove control.
     */
    public void removeDataBlock() {
        if (dataBlockRemoveHandler == null) {
            throw new IllegalStateException("No data block remove handler set");
        }
        dataBlockRemoveHandler.run();
    }

    /**
     * Picks a suggested question as the user would.
     *
     * @param prompt text of the picked question
     */
    public void pickPrompt(String prompt) {
        if (suggestedPromptHandler == null) {
            throw new IllegalStateException("No suggested prompt handler set");
        }
        suggestedPromptHandler.accept(prompt);
    }

    @Override
    public void showDataBlock(String caption, @Nullable String warning) {
        dataBlockShown = true;
        dataBlockCaption = caption;
        dataBlockWarning = warning;
    }

    @Override
    public void hideDataBlock() {
        dataBlockShown = false;
        dataBlockCaption = null;
        dataBlockWarning = null;
    }

    @Override
    public void setDataBlockRemoveHandler(@Nullable SerializableRunnable handler) {
        this.dataBlockRemoveHandler = handler;
    }

    @Override
    public void setSuggestedPrompts(List<String> prompts) {
        suggestedPrompts = List.copyOf(prompts);
    }

    @Override
    public void setSuggestedPromptHandler(@Nullable SerializableConsumer<String> handler) {
        this.suggestedPromptHandler = handler;
    }
}

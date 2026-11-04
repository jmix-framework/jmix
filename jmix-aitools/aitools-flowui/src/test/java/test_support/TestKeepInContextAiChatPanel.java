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

import io.jmix.aitoolsflowui.fragment.chat.HasKeepInContext;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A recording chat panel that also shows the assistant memory marks.
 */
public class TestKeepInContextAiChatPanel extends TestAiChatPanel implements HasKeepInContext {

    @Nullable
    public KeepInContextHandler keepInContextHandler;
    public final List<AiChatMessage> refreshedMessages = new ArrayList<>();

    /**
     * Asks to keep or release a message as the user would from the panel's control.
     *
     * @param message message the user acted on
     * @param keep    {@code true} to keep the message, {@code false} to release it
     */
    public void requestKeepInContext(AiChatMessage message, boolean keep) {
        if (keepInContextHandler == null) {
            throw new IllegalStateException("No keep-in-context handler set");
        }
        keepInContextHandler.onKeepInContextRequested(message, keep);
    }

    @Override
    public void setKeepInContextHandler(@Nullable KeepInContextHandler handler) {
        this.keepInContextHandler = handler;
    }

    @Override
    public void refreshKeepInContext(AiChatMessage message) {
        refreshedMessages.add(message);
    }
}

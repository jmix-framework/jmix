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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.aitools.tool.AiToolStatusUpdate;
import io.jmix.aitoolsflowui.fragment.chat.AiChatPanel;
import io.jmix.aitoolsflowui.fragment.chat.HasAiAvatarIcon;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * A chat panel that only records what the chat fragment told it.
 */
public class TestAiChatPanel extends Div implements AiChatPanel, HasAiAvatarIcon {

    @Nullable
    public Consumer<String> submitHandler;
    @Nullable
    public Runnable stopHandler;
    @Nullable
    public Runnable retryHandler;
    public final List<AiChatMessage> shownMessages = new ArrayList<>();
    public int showMessagesCalls;
    public boolean lockedWhileResponding = true;
    public boolean responseRunning;
    @Nullable
    public String failure;
    public boolean inputEnabled = true;
    public boolean inputVisible = true;
    @Nullable
    public SerializableSupplier<Component> avatarIconSupplier;

    /**
     * Submits the text as the user would from the panel's input.
     *
     * @param text the submitted text
     */
    public void submit(String text) {
        if (submitHandler != null) {
            submitHandler.accept(text);
        }
    }

    @Override
    public Component asComponent() {
        return this;
    }

    @Override
    public void setSubmitHandler(Consumer<String> handler) {
        this.submitHandler = handler;
    }

    @Override
    public void setStopHandler(Runnable handler) {
        this.stopHandler = handler;
    }

    @Override
    public void setRetryHandler(Runnable handler) {
        this.retryHandler = handler;
    }

    @Override
    public void focusInput() {
    }

    @Override
    public void restoreInputText(String text) {
    }

    @Override
    public void setInputEnabled(boolean enabled) {
        this.inputEnabled = enabled;
    }

    @Override
    public void setInputVisible(boolean visible) {
        this.inputVisible = visible;
    }

    @Override
    public boolean isInputLockedWhileResponding() {
        return lockedWhileResponding;
    }

    @Override
    public void showMessages(Collection<AiChatMessage> messages) {
        showMessagesCalls++;
        shownMessages.clear();
        shownMessages.addAll(messages);
    }

    @Override
    public void appendUserMessage(AiChatMessage message) {
        shownMessages.add(message);
    }

    @Override
    public void showResponseRunning() {
        responseRunning = true;
    }

    @Override
    public void showStatuses(List<AiToolStatusUpdate> statuses) {
    }

    @Override
    public void hideResponseRunning() {
        responseRunning = false;
    }

    @Override
    public void showResponseFailed(String errorText) {
        this.failure = errorText;
    }

    @Override
    public void onResponseCompleted(UUID assistantMessageId) {
    }

    @Override
    public void setAiAvatarIconSupplier(@Nullable SerializableSupplier<Component> avatarIconSupplier) {
        this.avatarIconSupplier = avatarIconSupplier;
    }
}

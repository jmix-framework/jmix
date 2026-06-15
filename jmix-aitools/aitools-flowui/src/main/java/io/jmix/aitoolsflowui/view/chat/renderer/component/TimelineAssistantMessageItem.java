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

package io.jmix.aitoolsflowui.view.chat.renderer.component;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.markdown.Markdown;
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.aitools.entity.ChatMessage;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

/**
 * Timeline row for a persisted assistant {@link ChatMessage}, rendering its content as Markdown.
 */
public class TimelineAssistantMessageItem extends AbstractTimelineItem {

    protected static final String ASSISTANT_MESSAGE_CN = "timeline-message-row-assistant";
    protected static final String ASSISTANT_MESSAGE_REFRESH_CN = "timeline-message-row-fresh";
    protected static final String ASSISTANT_MESSAGE_MARKDOWN_CN = "timeline-markdown";

    @Nullable
    protected SerializableSupplier<Component> avatarIconSupplier;

    /**
     * Renders the row for the given assistant message.
     *
     * @param message   assistant message to display
     * @param isFresh   whether to highlight the row as just generated
     * @param actorName actor display name shown in the header
     */
    public void setMessage(ChatMessage message, boolean isFresh, String actorName) {
        removeClassNames(ASSISTANT_MESSAGE_REFRESH_CN, ASSISTANT_MESSAGE_CN);

        addClassNames(ASSISTANT_MESSAGE_CN);

        if (isFresh) {
            addClassName(ASSISTANT_MESSAGE_REFRESH_CN);
        }

        initRow(actorName);

        body.add(createMessageContent(message));
    }

    private Component createMessageContent(ChatMessage message) {
        String content = Optional.ofNullable(message.getContent()).orElse("");
        Markdown markdown = new Markdown(content);
        markdown.addClassName(ASSISTANT_MESSAGE_MARKDOWN_CN);
        return markdown;
    }

    /**
     * Sets the supplier of the avatar icon shown for this row. The supplier must return a fresh
     * component on every call.
     *
     * @param avatarIconSupplier supplier of the avatar icon, or {@code null} to render no icon
     */
    public void setAiAvatarIconSupplier(@Nullable SerializableSupplier<Component> avatarIconSupplier) {
        this.avatarIconSupplier = avatarIconSupplier;
    }

    @Override
    protected Component createAvatar(String actorName) {
        AssistantAvatar avatar = new AssistantAvatar();
        if (avatarIconSupplier != null) {
            avatar.setIcon(avatarIconSupplier.get());
        }
        return avatar;
    }
}

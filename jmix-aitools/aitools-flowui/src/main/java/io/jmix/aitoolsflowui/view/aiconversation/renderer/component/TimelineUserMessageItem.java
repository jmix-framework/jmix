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

package io.jmix.aitoolsflowui.view.aiconversation.renderer.component;

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import io.jmix.aitools.entity.ChatMessage;

/**
 * A row representing a persisted user or assistant {@link ChatMessage}.
 * Assistant content is rendered as Markdown, user content as plain text.
 * <p>
 * Attachments and entity references are intentionally not supported in the
 * add-on (the entity model does not carry them).
 */
public class TimelineUserMessageItem extends AbstractTimelineItem {

    protected static final String USER_MESSAGE_CN = "timeline-message-row-user";
    protected static final String USER_MESSAGE_CONTENT_CN = "timeline-user-text";
    protected static final String AVATAR_CN = "timeline-avatar";

    public void setMessage(ChatMessage message, String actorName) {
        removeClassName(USER_MESSAGE_CN);

        addClassNames(USER_MESSAGE_CN);

        initRow(actorName);

        body.add(createMessageContent(message));
    }

    @Override
    protected Component createAvatar(String actorName) {
        Avatar userAvatar = new Avatar(actorName);
        userAvatar.addClassName(AVATAR_CN);
        return userAvatar;
    }

    private Component createMessageContent(ChatMessage message) {
        Span text = new Span(Strings.nullToEmpty(message.getContent()));
        text.addClassName(USER_MESSAGE_CONTENT_CN);
        return text;
    }
}

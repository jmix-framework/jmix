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

import com.google.common.base.Strings;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.Span;
import io.jmix.aitoolsflowui.model.AiChatMessage;

import java.util.Locale;

/**
 * A row representing a persisted user or assistant {@link AiChatMessage}.
 * Assistant content is rendered as Markdown, user content as plain text.
 */
public class TimelineUserMessageItem extends AbstractTimelineItem {

    protected static final String USER_MESSAGE_CN = "timeline-message-row-user";
    protected static final String USER_MESSAGE_CONTENT_CN = "timeline-user-text";
    protected static final String AVATAR_CN = "timeline-avatar";

    /**
     * Renders the row for the given message.
     *
     * @param message   message to display
     * @param actorName actor display name shown in the header
     */
    public void setMessage(AiChatMessage message, String actorName) {
        removeClassName(USER_MESSAGE_CN);

        addClassNames(USER_MESSAGE_CN);

        initRow(actorName);

        body.add(createMessageContent(message));
    }

    @Override
    protected Component createAvatar(String actorName) {
        Avatar userAvatar = new Avatar(actorName);
        userAvatar.setAbbreviation(avatarAbbreviation(actorName));
        userAvatar.addClassName(AVATAR_CN);
        return userAvatar;
    }

    /**
     * Builds the avatar initials from the actor name. The Jmix user
     * instance name embeds the login as {@code "First Last [username]"}; the
     * bracketed login is dropped so the circle shows clean initials (e.g.
     * {@code "JS"}), falling back to the login's first letter when no name is
     * present. Set explicitly so the Avatar does not derive noisy initials from
     * the whole string.
     */
    protected static String avatarAbbreviation(String actorName) {
        String source = Strings.nullToEmpty(actorName);
        String name = source.replaceAll("\\[.*?]", " ").trim();
        if (name.isEmpty()) {
            name = source.replace("[", " ").replace("]", " ").trim();
        }
        if (name.isEmpty()) {
            return "";
        }
        String[] words = name.split("\\s+");
        String abbreviation = words[0].charAt(0)
                + (words.length > 1 ? words[words.length - 1].substring(0, 1) : "");
        return abbreviation.toUpperCase(Locale.ROOT);
    }

    protected Component createMessageContent(AiChatMessage message) {
        Span text = new Span(Strings.nullToEmpty(message.getContent()));
        text.addClassName(USER_MESSAGE_CONTENT_CN);
        return text;
    }
}

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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.markdown.Markdown;
import io.jmix.aitools.entity.ChatMessage;

import java.util.Optional;

public class TimelineAssistantMessageItem extends AbstractTimelineItem {

    protected static final String ASSISTANT_MESSAGE_CN = "ai-timeline-message-row-assistant";
    protected static final String ASSISTANT_MESSAGE_REFRESH_CN = "ai-timeline-message-row-fresh";
    protected static final String ASSISTANT_MESSAGE_MARKDOWN_CN = "ai-timeline-markdown";

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

    @Override
    protected Component createAvatar(String actorName) {
        return new AssistantAvatar();
    }
}

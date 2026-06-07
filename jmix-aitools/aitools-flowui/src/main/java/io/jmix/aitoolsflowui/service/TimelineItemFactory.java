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

package io.jmix.aitoolsflowui.service;

import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitools.entity.ChatMessageType;
import io.jmix.aitoolsflowui.model.TimelineItem;
import io.jmix.aitoolsflowui.model.TimelineItemType;
import io.jmix.core.Metadata;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("aitols_TimelineItemFactory")
public class TimelineItemFactory {

    @Autowired
    protected Metadata metadata;

    public TimelineItem createUserItem(ChatMessage message) {
        TimelineItem userItem = metadata.create(TimelineItem.class);
        userItem.setMessage(message);
        userItem.setType(TimelineItemType.USER);
        return userItem;
    }

    public TimelineItem createAssistantItem(ChatMessage message) {
        TimelineItem assistantItem = metadata.create(TimelineItem.class);
        assistantItem.setMessage(message);
        assistantItem.setType(TimelineItemType.ASSISTANT);
        return assistantItem;
    }

    public TimelineItem createThinkingItem(ChatMessage message) {
        TimelineItem thinkingItem = metadata.create(TimelineItem.class);
        thinkingItem.setMessage(message);
        thinkingItem.setType(TimelineItemType.ASSISTANT_THINKING);
        return thinkingItem;
    }

    public List<TimelineItem> buildTimelineItems(@Nullable List<ChatMessage> messages) {
        if (messages == null) {
            return List.of();
        }
        return messages.stream()
                .map(this::createTimelineItem)
                .toList();
    }

    protected TimelineItem createTimelineItem(ChatMessage message) {
        ChatMessageType type = message.getType();
        if (ChatMessageType.ASSISTANT.equals(type) || ChatMessageType.TOOL.equals(type)) {
            return createAssistantItem(message);
        }
        return createUserItem(message);
    }
}

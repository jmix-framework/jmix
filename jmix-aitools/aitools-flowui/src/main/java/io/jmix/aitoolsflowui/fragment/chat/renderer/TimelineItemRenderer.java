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

package io.jmix.aitoolsflowui.fragment.chat.renderer;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.function.SerializableSupplier;
import io.jmix.aitoolsflowui.fragment.chat.AiChatFragment;
import io.jmix.aitoolsflowui.fragment.chat.HasKeepInContext.KeepInContextHandler;
import io.jmix.aitoolsflowui.fragment.chat.renderer.component.AbstractTimelineItem;
import io.jmix.aitoolsflowui.fragment.chat.renderer.component.TimelineAssistantMessageItem;
import io.jmix.aitoolsflowui.fragment.chat.renderer.component.TimelineAssistantThinkingMessageItem;
import io.jmix.aitoolsflowui.fragment.chat.renderer.component.TimelineUserMessageItem;
import io.jmix.aitoolsflowui.fragment.chat.support.ActorNameResolver;
import io.jmix.aitoolsflowui.fragment.chat.support.ChatDateTimeSupport;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItem;
import io.jmix.aitoolsflowui.fragment.chat.timeline.TimelineItemType;
import io.jmix.aitoolsflowui.icon.AiIconProvider;
import io.jmix.aitoolsflowui.model.AiChatMessage;
import io.jmix.aitoolsflowui.model.AiChatMessageType;
import io.jmix.core.Messages;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Painting a single {@link TimelineItem} row for the conversation virtual list.
 */
@FragmentDescriptor("timeline-item-renderer-fragment.xml")
public class TimelineItemRenderer extends FragmentRenderer<VerticalLayout, TimelineItem> {

    @Autowired
    protected ActorNameResolver actorNameResolver;
    @Autowired
    protected ChatDateTimeSupport chatDateTimeSupport;
    @Autowired
    protected Messages messages;
    @Autowired
    protected AiIconProvider iconProvider;

    // Per-instance lazy caches for the three inner row shapes. Each entry is
    // initialised on first use and then reused for every subsequent setItem
    // call of the matching type on this renderer instance.
    @Nullable
    private TimelineUserMessageItem userItem;
    @Nullable
    private TimelineAssistantMessageItem assistantItem;
    @Nullable
    private TimelineAssistantThinkingMessageItem thinkingItem;
    @Nullable
    private TimelineItemType itemType;

    @Nullable
    protected SerializableSupplier<Component> aiAvatarIconSupplier;

    @Nullable
    protected KeepInContextHandler keepInContextHandler;

    /**
     * Sets the handler of keep requests raised by the pin control of the rows this renderer draws. Without a handler
     * the rows get no control.
     *
     * @param keepInContextHandler handler of keep requests, or {@code null} for rows without the control
     */
    public void setKeepInContextHandler(@Nullable KeepInContextHandler keepInContextHandler) {
        this.keepInContextHandler = keepInContextHandler;
    }

    /**
     * Sets the supplier of the assistant avatar icon for the rows this renderer draws. It must be set before the
     * first assistant row is drawn: the icon is taken once, when that row is created.
     *
     * @param avatarIconSupplier supplier of the icon, or {@code null} to use the default
     */
    public void setAiAvatarIconSupplier(@Nullable SerializableSupplier<Component> avatarIconSupplier) {
        this.aiAvatarIconSupplier = avatarIconSupplier;
    }

    @Override
    public void setItem(@Nullable TimelineItem item) {
        // Skip super.setItem()
        this.item = item;

        if (item == null || item.getType() == null) {
            getContent().removeAll();
            itemType = null;
            return;
        }

        TimelineItemType type = item.getType();
        AbstractTimelineItem messageItem = getOrCreateMessageItem(type);

        if (type != itemType) {
            getContent().removeAll();
            getContent().add(messageItem);
            itemType = type;
        }

        applyData(messageItem, item);
    }

    private AbstractTimelineItem getOrCreateMessageItem(TimelineItemType type) {
        return switch (type) {
            case USER -> {
                if (userItem == null) {
                    userItem = uiComponents.create(TimelineUserMessageItem.class);
                }
                yield userItem;
            }
            case ASSISTANT -> {
                if (assistantItem == null) {
                    assistantItem = uiComponents.create(TimelineAssistantMessageItem.class);
                    assistantItem.setAiAvatarIconSupplier(resolveAiAvatarIconSupplier());
                }
                yield assistantItem;
            }
            case ASSISTANT_THINKING -> {
                if (thinkingItem == null) {
                    thinkingItem = uiComponents.create(TimelineAssistantThinkingMessageItem.class);
                    thinkingItem.setAiAvatarIconSupplier(resolveAiAvatarIconSupplier());
                }
                yield thinkingItem;
            }
        };
    }

    private void applyData(AbstractTimelineItem messageItem, TimelineItem item) {
        AiChatMessage message = item.getMessage();
        switch (item.getType()) {
            case USER -> ((TimelineUserMessageItem) messageItem).setMessage(message,
                    actorNameResolver.resolve(message, messages.getMessage(getMessageGroup(), "timelineItemRenderer.defaultActorName"))
            );
            case ASSISTANT -> {
                boolean fresh = Boolean.TRUE.equals(item.getFresh());
                ((TimelineAssistantMessageItem) messageItem).setMessage(
                        message,
                        fresh,
                        messages.getMessage(getMessageGroup(), "timelineItemRenderer.assistantName")
                );
                if (fresh) {
                    // Consume the marker: animate once, do not replay when the row is re-rendered.
                    item.setFresh(false);
                }
            }
            case ASSISTANT_THINKING -> ((TimelineAssistantThinkingMessageItem) messageItem).setThinking(
                    item,
                    messages.getMessage(getMessageGroup(), "timelineItemRenderer.assistantName"),
                    messages.getMessage(getMessageGroup(), "timelineItemRenderer.thinkingIndicator")
            );
        }

        messageItem.setTime(
                chatDateTimeSupport.formatInUserZone(
                        message != null ? message.getCreatedDate() : null));

        if (item.getType() == TimelineItemType.ASSISTANT_THINKING) {
            hideKeepControl(messageItem);
        } else {
            applyKeepControl(messageItem, message);
        }
    }

    protected void applyKeepControl(AbstractTimelineItem messageItem, @Nullable AiChatMessage message) {
        KeepInContextHandler handler = keepInContextHandler;
        if (message == null || !isKeepable(message) || handler == null) {
            hideKeepControl(messageItem);
            return;
        }

        messageItem.setKept(Boolean.TRUE.equals(message.getKeepInContext()));
        messageItem.setKeepClickHandler(keep -> handler.onKeepInContextRequested(message, keep));
        messageItem.setKeepVisible(true);
    }

    protected void hideKeepControl(AbstractTimelineItem messageItem) {
        messageItem.setKeepVisible(false);
        messageItem.setKept(false);
        messageItem.setKeepClickHandler(null);
    }

    /**
     * Tells whether the control applies to the message: only user and assistant messages that belong to a stored
     * conversation can be kept, not tool results or transient rows such as a failed response.
     *
     * @param message message of the row
     * @return {@code true} if the row gets the keep control
     */
    protected boolean isKeepable(AiChatMessage message) {
        AiChatMessageType type = message.getType();

        return (type == AiChatMessageType.USER || type == AiChatMessageType.ASSISTANT)
                && message.getConversation() != null;
    }

    protected SerializableSupplier<Component> resolveAiAvatarIconSupplier() {
        return aiAvatarIconSupplier != null ? aiAvatarIconSupplier : iconProvider::createAvatarIcon;
    }

    protected String getMessageGroup() {
        return AiChatFragment.class.getPackageName();
    }
}

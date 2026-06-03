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

package io.jmix.aitoolsflowui.view.aiconversation.renderer;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.jmix.aitools.entity.ChatMessage;
import io.jmix.aitoolsflowui.model.TimelineItem;
import io.jmix.aitoolsflowui.model.TimelineItemType;
import io.jmix.aitoolsflowui.view.aiconversation.support.ActorNameResolver;
import io.jmix.aitoolsflowui.view.aiconversation.renderer.component.AbstractTimelineItem;
import io.jmix.aitoolsflowui.view.aiconversation.renderer.component.TimelineAssistantMessageItem;
import io.jmix.aitoolsflowui.view.aiconversation.renderer.component.TimelineAssistantThinkingMessageItem;
import io.jmix.aitoolsflowui.view.aiconversation.renderer.component.TimelineUserMessageItem;
import io.jmix.core.Messages;
import io.jmix.core.metamodel.datatype.DatatypeFormatter;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.fragment.FragmentDescriptor;
import io.jmix.flowui.fragmentrenderer.FragmentRenderer;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Jmix {@link FragmentRenderer} painting a single {@link TimelineItem} row
 * for the conversation virtual list. Wired declaratively in the host
 * fragment descriptor via {@code <fragmentRenderer class="..."/>}.
 * <p>
 * <b>Instance reuse.</b> Vaadin's virtual list pools the fragment itself —
 * {@link #setItem(TimelineItem)} is called per row on the same instance as
 * the user scrolls. We additionally cache up to three inner item components
 * (user / assistant / thinking) per fragment instance so the inner component
 * is reused when consecutive {@code setItem} calls land the same row type
 * at the same position (e.g. when a thinking row is refreshed for every
 * incoming status update). Only a type change triggers a DOM swap; same-type
 * updates just push fresh data into the already-mounted inner component.
 * <p>
 * We do not use {@code @RendererItemContainer} / declarative data binding
 * because the three row shapes are structurally different (markdown vs
 * plain span vs shimmer + status list) and cannot be expressed as a single
 * data-bound layout.
 */
@FragmentDescriptor("timeline-item-renderer-fragment.xml")
public class TimelineItemRenderer extends FragmentRenderer<VerticalLayout, TimelineItem> {

    /**
     * Message group of the keys used for the row labels. The renderer
     * descriptor lives in a different package than the host fragment, so its
     * own auto-derived {@code MessageBundle} would not see these keys — we
     * resolve them through {@link Messages} with the explicit group instead.
     */
    public static final String MESSAGE_GROUP = "io.jmix.aitoolsflowui.view.aiconversation";

    @Autowired
    protected UiComponents uiComponents;
    @Autowired
    protected ActorNameResolver actorNameResolver;
    @Autowired
    protected DatatypeFormatter datatypeFormatter;
    @Autowired
    protected Messages messages;

    // Per-instance lazy caches for the three inner row shapes. Each entry is
    // initialised on first use and then reused for every subsequent setItem
    // call of the matching type on this renderer instance.
    @Nullable
    private TimelineUserMessageItem userItem;
    @Nullable
    private TimelineAssistantMessageItem assistantItem;
    @Nullable
    private TimelineAssistantThinkingMessageItem thinkingItem;

    /**
     * Type of the inner item currently mounted under {@link #getContent()}.
     * {@code null} when nothing is mounted (initial state, or after being
     * called with a {@code null} timeline item). Drives the "swap inner
     * component only on type change" logic.
     */
    @Nullable
    private TimelineItemType itemType;

    @Override
    public void setItem(@Nullable TimelineItem item) {
        // Skip super.setItem — we have no @RendererItemContainer, so the base
        // implementation would only log an INFO line per call (and we are
        // called per row × every list refresh).
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
                }
                yield assistantItem;
            }
            case ASSISTANT_THINKING -> {
                if (thinkingItem == null) {
                    thinkingItem = uiComponents.create(TimelineAssistantThinkingMessageItem.class);
                }
                yield thinkingItem;
            }
        };
    }

    private void applyData(AbstractTimelineItem messageItem, TimelineItem item) {
        ChatMessage message = item.getMessage();
        switch (item.getType()) {
            case USER -> ((TimelineUserMessageItem) messageItem).setMessage(message,
                    actorNameResolver.resolve(message, messages.getMessage(MESSAGE_GROUP, "defaultActorName"))
            );
            case ASSISTANT -> ((TimelineAssistantMessageItem) messageItem).setMessage(
                    message,
                    Boolean.TRUE.equals(item.getFresh()),
                    messages.getMessage(MESSAGE_GROUP, "assistantName")
            );
            case ASSISTANT_THINKING -> ((TimelineAssistantThinkingMessageItem) messageItem).setThinking(
                    item,
                    messages.getMessage(MESSAGE_GROUP, "assistantName"),
                    messages.getMessage(MESSAGE_GROUP, "thinkingIndicator")
            );
        }

        messageItem.setTime(
                datatypeFormatter.formatOffsetDateTime(
                        message != null ? message.getCreatedDate() : null));
    }
}
